package net.alvisssss.advancedmissile.entity.custom;

import net.alvisssss.advancedmissile.AdvancedMissile;
import net.alvisssss.advancedmissile.entity.ModEntities;
import net.alvisssss.advancedmissile.item.ModItems;
import net.alvisssss.advancedmissile.util.RealisticExplosion;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.Difficulty;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class MissileEntity extends ProjectileEntity {
    @Nullable
    private Entity target;

    private double targetX;
    private double targetY;
    private double targetZ;
    private double steeringForce = 0.4;

    @Nullable
    private UUID targetUuid;

    private boolean hit = false;
    private boolean hasTarget;
    private boolean isPrecise = false;

    private int cooldownTimer = 20;
    private int deactivateCountdown = 200;
    private int situation = 0;

    private float additionalPower = 0.0f;
    private float speed;

    private String code;

    private ItemStack missileStack = new ItemStack(ModItems.JAVELIN_MISSILE) ;


    public MissileEntity(EntityType<MissileEntity> missileEntityEntityType, World world) {
        super(missileEntityEntityType, world);
    }

    public MissileEntity(World world, NbtCompound nbt) {

        this(ModEntities.MISSILE, world);

        NbtCompound nbtCompound = this.missileStack.getOrCreateNbt();
        nbtCompound.copyFrom(nbt);

        if (nbt.contains("isPrecise")) {
            this.isPrecise = nbt.getBoolean("isPrecise");
        }

        if (nbt.contains("code")) {
            this.code = nbt.getString("code");
        }
        if (nbt.contains("fuel_count")) {
            this.deactivateCountdown += nbt.getInt("fuel_count") * 20;
        }

        if (nbt.contains("tnt_count")) {
            this.additionalPower += (float) nbt.getInt("tnt_count");
        }

        if (nbt.contains("speed")) {
            this.speed = nbt.getFloat("speed");
        }

        if (nbt.contains("isBlock")) {
            if (!nbt.getBoolean("isBlock")) {
                if (nbt.contains("id")) {
                    this.target = world.getEntityById(nbt.getInt("id"));
                    this.situation = 1;
                }
                if (nbt.contains("Target")) {
                    this.targetUuid = nbt.getUuid("Target");
                    this.situation = 1;
                }
            } else {
                this.situation = 2;
            }
        } else {
            this.situation = 3;
        }

        if (nbt.contains("TXD") && nbt.contains("TYD") && nbt.contains("TZD")) {

            this.targetX = nbt.getDouble("TXD");
            this.targetY = nbt.getDouble("TYD");
            this.targetZ = nbt.getDouble("TZD");

        }
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        return EntityDimensions.fixed(0.5f, 0.5f);
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.HOSTILE;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (this.target != null) {
            nbt.putUuid("Target", this.target.getUuid());
        }

        nbt.putInt("cooldownTimer", this.cooldownTimer);
        nbt.putInt("deactivateCountdown", this.deactivateCountdown);
        nbt.putFloat("tntPower", this.additionalPower);
        nbt.putFloat("speed", this.speed);
        nbt.putDouble("TXD", this.targetX);
        nbt.putDouble("TYD", this.targetY);
        nbt.putDouble("TZD", this.targetZ);
        nbt.putBoolean("hasTarget", this.hasTarget);

        nbt.put("chunkPos", toNbtList(this.getChunkPos().x, this.getChunkPos().z));
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.containsUuid("Target")) {
            this.targetUuid = nbt.getUuid("Target");
        }
        if (nbt.contains("cooldownTimer")) {
            this.cooldownTimer = nbt.getInt("cooldownTimer");
        }
        if (nbt.contains("deactivateCountdown")) {
            this.deactivateCountdown = nbt.getInt("deactivateCountdown");
        }
        if (nbt.contains("tntPower")) {
            this.additionalPower = nbt.getFloat("tntPower");
        }

        this.speed = nbt.getFloat("speed");
        this.targetX = nbt.getDouble("TXD");
        this.targetY = nbt.getDouble("TYD");
        this.targetZ = nbt.getDouble("TZD");
        if (nbt.contains("hasTarget")) {
            this.hasTarget = nbt.getBoolean("hasTarget");
        }

        if (nbt.contains("chunkPos")) {
            NbtList list = nbt.getList("chunkPos", NbtElement.INT_TYPE);
            ServerWorld world = (ServerWorld) this.getEntityWorld();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    ChunkPos chunkPos = new ChunkPos(list.getInt(0) + dx, list.getInt(1) + dz);
                    world.getChunkManager().addTicket(ChunkTicketType.PORTAL, chunkPos, 10, this.getBlockPos());
                }
            }
        }

    }

    @Override
    protected void initDataTracker() {
    }

    @Override
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == Difficulty.PEACEFUL) {
            this.discard();
        }
    }

    private boolean hasDirectViewOfTarget(Vec3d targetPos) {
        HitResult result;
        result = this.getWorld().raycast(new RaycastContext(this.getPos(), targetPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));
        return result == null || result.getType() == HitResult.Type.MISS;
    }

    private void computeSteeringForce(Vec3d targetDirection, Vec3d currentDirection) {
        double dotProduct = currentDirection.dotProduct(targetDirection);
        double angle = Math.acos(dotProduct / (currentDirection.length() * targetDirection.length())) / Math.PI;
        if (Double.isNaN(angle)) {angle = 1.0;}
        this.steeringForce = Math.abs(angle) * 0.6 + 0.25;
    }

    private void moveTowardsTarget(boolean isEntity) {

        Vec3d targetPos;

        if (isEntity) {
            targetPos = this.target.getPos();
        } else {
            targetPos = new Vec3d(this.targetX, this.targetY, this.targetZ);
        }

        Vec3d currentPos = this.getPos();

        Vec3d targetDirection = targetPos.subtract(currentPos);
        Vec3d currentDirection = this.getVelocity();

        computeSteeringForce(targetDirection, currentDirection);

        targetDirection.normalize();
        currentDirection.normalize();

        Vec3d steering = targetDirection.subtract(currentDirection).normalize().multiply(this.steeringForce);

        Vec3d newVelocity = this.getVelocity().add(steering).normalize().multiply(this.speed);

        this.setVelocity(newVelocity);

    }
    private void updateVelocity(int control) {
        switch (control) {
            default:
                this.setVelocity(this.getVelocity().add(0.0, -0.04, 0.0));
                break;
            case 1, 2:
                break;
        }
    }

    private void handleSolidBlockCollision() {
        BlockPos blockPos = this.getBlockPos();
        BlockState blockState = this.getWorld().getBlockState(blockPos);

        if (!blockState.isAir() && !blockState.getFluidState().isEmpty()) {
            VoxelShape collisionShape = blockState.getCollisionShape(this.getWorld(), blockPos);

            if (!collisionShape.isEmpty()) {
                Box missileBox = this.getBoundingBox();
                Vec3d motion = this.getVelocity();

                // Calculate intersection with the collision shape
                Optional<Vec3d> closestPointOptional = collisionShape.getClosestPointTo(motion);

                if (closestPointOptional.isPresent()) {

                    Vec3d closestPoint = closestPointOptional.get();

                    Vec3d newMotion = this.getPos().add(motion).subtract(closestPoint);
                    // Check if collision resolves mainly in horizontal direction
                    double dx = newMotion.x - motion.x;
                    double dy = newMotion.y - motion.y;
                    double dz = newMotion.z - motion.z;

                    // Adjust missile's motion based on collision resolution
                    if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > Math.abs(dz)) {
                        newMotion = newMotion.subtract(dx, 0, 0); // Cancel horizontal x-motion
                    } else if (Math.abs(dz) > Math.abs(dx) && Math.abs(dz) > Math.abs(dy)) {
                        newMotion = newMotion.subtract(0, 0, dz); // Cancel horizontal z-motion
                    } else if (Math.abs(dy) > 0.01 && Math.abs(dy) > Math.abs(dx) && Math.abs(dy) > Math.abs(dz)) {
                        newMotion = newMotion.subtract(0, dy, 0); // Cancel vertical y-motion if hitting ground at an angle
                    }

                    // Update missile's position and velocity
                    this.updatePosition(this.getX() + newMotion.x, this.getY() + newMotion.y, this.getZ() + newMotion.z);
                    this.setVelocity(newMotion);
                }
            }
        }
    }

    @Override
    public void tick() {
        Vec3d vec3d;
        super.tick();

        if (!this.getWorld().isClient) {

            if (this.getPos() == null) {
                this.discard();
            }

            // Save positions and velocities

            if (this.target == null && this.targetUuid != null) {
                this.target = ((ServerWorld)this.getWorld()).getEntity(this.targetUuid);
                if (this.target == null) {
                    this.targetUuid = null;
                }
            }

            ServerWorld world = (ServerWorld) this.getEntityWorld();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    ChunkPos chunkPos = new ChunkPos(this.getChunkPos().x + dx, this.getChunkPos().z + dz);
                    world.getChunkManager().addTicket(ChunkTicketType.PORTAL, chunkPos, 10, this.getBlockPos());
                }
            }

            AdvancedMissile.LOGGER.info(this.getX() + String.valueOf(this.getY()) + this.getZ());

            // Case entity and entity valid.
            if (this.situation == 1 && !this.hit && !(this.target == null || !this.target.isAlive() || this.target instanceof PlayerEntity && this.target.isSpectator())) {
                if (hasDirectViewOfTarget(this.target.getPos()) && this.cooldownTimer <= 0 && this.deactivateCountdown > 0) {

                    moveTowardsTarget(true);

                } else {

                    this.setVelocity(this.getVelocity().add(0.0, -0.04, 0.0));
                    this.cooldownTimer--;
                }

                if (this.deactivateCountdown >= 0) {
                    this.deactivateCountdown--;
                }

            } else if (this.situation == 2 && !this.hit) { // Case block

                if (this.cooldownTimer <= 0 && this.deactivateCountdown > 0) {

                    moveTowardsTarget(false);

                } else {
                    this.setVelocity(this.getVelocity().add(0.0, -0.04, 0.0));
                    cooldownTimer--;
                }

                if (this.deactivateCountdown >= 0) {
                    this.deactivateCountdown--;
                }

                double distance = this.squaredDistanceTo(new Vec3d(MathHelper.floor(this.targetX) + 0.5, MathHelper.floor(this.targetY) + 0.5, MathHelper.floor(this.targetZ) + 0.5));
                if (distance <= 9.0) {
                    this.hit = true;
                }

            } else { // No target
                this.setVelocity(this.getVelocity().add(0.0, -0.04, 0.0));
                if (this.cooldownTimer > 0) cooldownTimer--;
            }


            HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
            if (hitResult.getType() != HitResult.Type.MISS) {
                this.onCollision(hitResult);
            }
        }

        this.checkBlockCollision();
        vec3d = this.getVelocity();
        this.setPosition(this.getX() + vec3d.x, this.getY() + vec3d.y, this.getZ() + vec3d.z);
        ProjectileUtil.setRotationFromVelocity(this, ((float) this.steeringForce));

        if (this.getWorld().isClient) {
            this.getWorld().addParticle(ParticleTypes.CLOUD, this.getX() - vec3d.x, this.getY() - vec3d.y + 0.15, this.getZ() - vec3d.z,
                    this.getVelocity().x * -0.1, this.getVelocity().y * -0.1, this.getVelocity().z * -0.1);
        }
    }

    @Override
    protected boolean canHit(Entity entity) {
        return super.canHit(entity) && !entity.noClip;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public boolean shouldRender(double distance) {
        return distance < 16384.0;
    }

    @Override
    public float getBrightnessAtEyes() {
        return 1.0f;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        AdvancedMissile.LOGGER.info("Entity Hit"); // Not calling because it missed...
        if (this.cooldownTimer <= 0) {
            new RealisticExplosion(this.getEntityWorld(), this, this.getX(), this.getBodyY(0.0625), this.getZ(), this.additionalPower, null, entityHitResult.getEntity(), this.isPrecise);
        }
        this.hit = true;
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);

        MinecraftClient.getInstance().player.sendMessage(Text.literal("End Position: " + this.getPos()));
        MinecraftClient.getInstance().player.sendMessage(Text.literal(" Time: " + this.getWorld().getTimeOfDay()));

        BlockPos blockPos = blockHitResult.getBlockPos();
        BlockState blockState = this.getWorld().getBlockState(blockPos);

        if (blockState.getFluidState().isEmpty()) {
            if (this.cooldownTimer <= 0) {
                new RealisticExplosion(this.getEntityWorld(), this, this.getX(), this.getBodyY(0.0625), this.getZ(), this.additionalPower, blockPos, null, this.isPrecise);
            }
            this.hit = true;
        }
    }
    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        this.destroy();
    }
    private void destroy() {
        this.discard();
        this.getWorld().emitGameEvent(GameEvent.ENTITY_DAMAGE, this.getPos(), GameEvent.Emitter.of(this));
    }



    @Override
    public boolean canHit() {
        return true;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (!this.getWorld().isClient) {
            this.playSound(SoundEvents.ENTITY_SHULKER_BULLET_HURT, 1.0f, 1.0f);
            ((ServerWorld)this.getWorld()).spawnParticles(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 15, 0.2, 0.2, 0.2, 0.0);
            this.destroy();
        }
        return true;
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        double d = packet.getVelocityX();
        double e = packet.getVelocityY();
        double f = packet.getVelocityZ();
        this.setVelocity(d, e, f);
    }
}