package net.alvisssss.advancedmissile.entity.custom;

import net.alvisssss.advancedmissile.entity.ModEntities;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
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
import net.minecraft.world.Difficulty;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

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
    private Vec3d lastPos;

    private boolean launched = false;
    private boolean hit = false;
    private boolean hasTarget;

    private int cooldownTimer = 20;
    private int deactivateCountdown = 400;

    private float additionalPower = 0.0f;
    private float speed;


    public MissileEntity(EntityType<MissileEntity> missileEntityEntityType, World world) {
        super(missileEntityEntityType, world);
    }

    public MissileEntity(World world, LivingEntity owner, NbtCompound nbt) {
        this(ModEntities.MISSILE, world);
        this.setOwner(owner);

        Direction playerFacing = owner.getHorizontalFacing();
        double offsetX = playerFacing.getOffsetX() * 0.5;
        double offsetY = playerFacing.getOffsetY() * 0.5 + owner.getEyeHeight(owner.getPose());
        double offsetZ = playerFacing.getOffsetZ() * 0.5;

        this.refreshPositionAndAngles(owner.getPos().x + offsetX, owner.getPos().y + offsetY, owner.getPos().z + offsetZ, this.getYaw(), this.getPitch());
        this.lastPos = new Vec3d(owner.getPos().x + offsetX, owner.getPos().y + offsetY, owner.getPos().z + offsetZ);


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
                }
                if (nbt.contains("Target")) {
                    this.targetUuid = nbt.getUuid("Target");
                }
            }
            this.hasTarget = true;
        } else {
            this.hasTarget = false;
        }

        if (nbt.contains("TXD") && nbt.contains("TYD") && nbt.contains("TZD")) {

            this.targetX = nbt.getDouble("TXD");
            this.targetY = nbt.getDouble("TYD");
            this.targetZ = nbt.getDouble("TZD");

        }

    }


    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.HOSTILE;
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
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
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
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
        this.additionalPower = nbt.getFloat("tntPower");
        this.speed = nbt.getFloat("speed");
        this.targetX = nbt.getDouble("TXD");
        this.targetY = nbt.getDouble("TYD");
        this.targetZ = nbt.getDouble("TZD");
        if (nbt.contains("hasTarget")) {
            this.hasTarget = nbt.getBoolean("hasTarget");
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
        this.steeringForce = (1.0 - angle) * 0.5 + 0.25;
    }

    private void moveTowardsTarget(boolean isEntity) {

        Vec3d targetPos;

        if (isEntity) {
            targetPos = this.target.getPos();
        } else {
            targetPos = new Vec3d(MathHelper.floor(this.targetX) + 0.5, MathHelper.floor(this.targetY) + 0.5, MathHelper.floor(this.targetZ) + 0.5);
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

    @Override
    public void tick() {
        Vec3d vec3d;
        super.tick();

        if (!this.getWorld().isClient) {
            ServerWorld world = ((ServerWorld) this.getEntityWorld());

            // Initializing vectors again.
            if (!this.launched) {
                if (this.lastPos == null || this.hit) {
                    this.discard();
                    return;
                } else {
                    refreshPositionAndAngles(this.lastPos.x, this.lastPos.y, this.lastPos.z, this.getYaw(), this.getPitch());
                }
                this.launched = true;
            }

            // Try to retrieve the target entity from the UUID.
            if (this.target == null && this.targetUuid != null) {
                this.target = ((ServerWorld)this.getWorld()).getEntity(this.targetUuid);
                if (this.target == null) {
                    this.targetUuid = null;
                }
            }

            // An entity
            if (!this.hit && !(this.target == null || !this.target.isAlive() || this.target instanceof PlayerEntity && this.target.isSpectator())) {

                if (hasDirectViewOfTarget(this.target.getPos()) && this.cooldownTimer <= 0 && this.deactivateCountdown > 0) {

                    moveTowardsTarget(true);

                } else {

                    this.setVelocity(this.getVelocity().add(0.0, -0.04, 0.0));
                    this.cooldownTimer--;
                }

                if (this.deactivateCountdown >= 0) {
                    this.deactivateCountdown--;
                }

            } else if (!this.hit && this.hasTarget) { // A block

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
                if (distance <= 2.0) {
                    this.hit = true;
                }

            } else { // No target
                this.setVelocity(this.getVelocity().add(0.0, -0.04, 0.0));
            }


            HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
            if (hitResult.getType() != HitResult.Type.MISS) {
                this.onCollision(hitResult);
            }

        }


        // Best not to touch. Setting the new position of the entity.
        this.checkBlockCollision();
        vec3d = this.getVelocity();
        this.setPosition(this.getX() + vec3d.x, this.getY() + vec3d.y, this.getZ() + vec3d.z);
        ProjectileUtil.setRotationFromVelocity(this, 0.5f);

        if (this.getWorld().isClient) {
            this.getWorld().addParticle(ParticleTypes.LARGE_SMOKE, this.getX() - vec3d.x, this.getY() - vec3d.y + 0.15, this.getZ() - vec3d.z, 0.0, 0.0, 0.0);
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

        this.getWorld().createExplosion(this, this.getX(), this.getBodyY(0.0625), this.getZ(), 4.0f + this.additionalPower, World.ExplosionSourceType.TNT);
        this.hit = true;
        this.discard();
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);

        BlockPos blockPos = blockHitResult.getBlockPos();
        BlockState blockState = this.getWorld().getBlockState(blockPos);

        if (blockState.getFluidState().isEmpty()) {
            this.getWorld().createExplosion(this, this.getX(), this.getBodyY(0.0625), this.getZ(), 4.0f + this.additionalPower, World.ExplosionSourceType.TNT);
            this.hit = true;
            this.discard();
        }
    }

    private void destroy() {
        this.discard();
        this.getWorld().emitGameEvent(GameEvent.ENTITY_DAMAGE, this.getPos(), GameEvent.Emitter.of(this));
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        this.destroy();
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