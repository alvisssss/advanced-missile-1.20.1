package net.alvisssss.advancedmissile.entity.custom;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import net.alvisssss.advancedmissile.entity.ModEntities;
import net.alvisssss.advancedmissile.item.ModItems;
import net.fabricmc.fabric.impl.object.builder.FabricEntityType;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.UUID;
/*

public class MissileEntity extends ShulkerBulletEntity {


    public MissileEntity(EntityType<MissileEntity> missileEntityEntityType, World world) {
        super(missileEntityEntityType, world);
    }

    public MissileEntity(World world, LivingEntity owner, Entity target, Direction.Axis axis) {
        super(world, owner, target, axis);
    }

}

 */

public class MissileEntity extends ProjectileEntity {
    @Nullable
    private Entity target;
    @Nullable
    private Direction direction;
    private int stepCount;
    private double targetX;
    private double targetY;
    private double targetZ;
    @Nullable
    private UUID targetUuid;
    private Vec3d lastPos;
    private boolean launched = false;
    private boolean hit = false;
    private int cooldownTimer = 20;
    private int despawnTimer;


    public MissileEntity(EntityType<MissileEntity> missileEntityEntityType, World world) {
        super(missileEntityEntityType, world);
    }

    public MissileEntity(World world, LivingEntity owner, Entity target, NbtCompound nbt) {
        this(ModEntities.MISSILE, world);
        this.setOwner(owner);
        BlockPos blockPos = owner.getBlockPos();
        double d = (double)blockPos.getX() + 0.5;
        double e = (double)blockPos.getY() + 0.5;
        double f = (double)blockPos.getZ() + 0.5;

        Direction playerFacing = owner.getHorizontalFacing();
        double offsetX = playerFacing.getOffsetX() * 0.5;
        double offsetY = playerFacing.getOffsetY() * 1.0;
        double offsetZ = playerFacing.getOffsetZ() * 0.5;

        this.refreshPositionAndAngles(owner.getPos().x + offsetX, owner.getPos().y + offsetY, owner.getPos().z + offsetZ, this.getYaw(), this.getPitch());

        this.target = target;
        this.direction = Direction.UP;
        this.lastPos = this.getSyncedPos();
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
        if (this.direction != null) {
            nbt.putInt("Dir", this.direction.getId());
        }
        nbt.putInt("Steps", this.stepCount);
        nbt.putDouble("TXD", this.targetX);
        nbt.putDouble("TYD", this.targetY);
        nbt.putDouble("TZD", this.targetZ);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.stepCount = nbt.getInt("Steps");
        this.targetX = nbt.getDouble("TXD");
        this.targetY = nbt.getDouble("TYD");
        this.targetZ = nbt.getDouble("TZD");
        if (nbt.contains("Dir", NbtElement.NUMBER_TYPE)) {
            this.direction = Direction.byId(nbt.getInt("Dir"));
        }
        if (nbt.containsUuid("Target")) {
            this.targetUuid = nbt.getUuid("Target");
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
        // Check if there are any obstacles between the entity and the target
        if (targetPos != null) {
            result = this.getWorld().raycast(new RaycastContext(this.getPos(), targetPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));
        } else {
            if (!this.getWorld().isClient) {
                ServerWorld serverWorld = (ServerWorld) this.getWorld();
                serverWorld.getPlayers().forEach(player -> {player.sendMessage(Text.literal("no targetPos"));});
            }
            return false;

        }
        // If the result is null, there are no obstacles, so return true
        return result == null || result.getType() == HitResult.Type.MISS;
    }

    @Override
    public void tick() {
        Vec3d vec3d;
        super.tick();
        if (!this.getWorld().isClient) {
            if (!this.launched) {
                if (this.lastPos == null || this.hit) {
                    this.discard();
                    return;
                } else {
                    this.setPosition(this.lastPos);
                }
                this.launched = true;
            }

            if (this.target == null && this.targetUuid != null) {
                this.target = ((ServerWorld)this.getWorld()).getEntity(this.targetUuid);
                if (this.target == null) {
                    this.targetUuid = null;
                }
            }
            if (!(this.target == null || !this.target.isAlive() || this.target instanceof PlayerEntity && this.target.isSpectator())) {

                if (hasDirectViewOfTarget(this.target.getPos()) && this.cooldownTimer <= 0) {
                    Vec3d targetPos = this.target.getPos();
                    Vec3d velocity = new Vec3d(targetPos.x - this.getX(), targetPos.y + (double)this.target.getHeight() * 0.5 - this.getY(), targetPos.z - this.getZ()).normalize().multiply(1);
                    this.setVelocity(velocity);
                } else {
                    this.targetX = MathHelper.clamp(this.targetX * 1.025, -1.0, 1.0);
                    this.targetY = MathHelper.clamp(this.targetY * 1.025, -1.0, 1.0);
                    this.targetZ = MathHelper.clamp(this.targetZ * 1.025, -1.0, 1.0);
                    vec3d = this.getVelocity();
                    this.setVelocity(vec3d.add((this.targetX - vec3d.x) * 0.2, (this.targetY - vec3d.y) * 0.2, (this.targetZ - vec3d.z) * 0.2));

                    --this.cooldownTimer;
                }
            } else if (!this.hasNoGravity()) {
                this.setVelocity(this.getVelocity().add(0.0, -0.04, 0.0));
            }
            HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
            if (hitResult.getType() != HitResult.Type.MISS) {
                this.onCollision(hitResult);
            }
        }
        this.checkBlockCollision();
        vec3d = this.getVelocity();
        this.setPosition(this.getX() + vec3d.x, this.getY() + vec3d.y, this.getZ() + vec3d.z);
        ProjectileUtil.setRotationFromVelocity(this, 0.5f);

        if (this.getWorld().isClient) {
            this.getWorld().addParticle(ParticleTypes.END_ROD, this.getX() - vec3d.x, this.getY() - vec3d.y + 0.15, this.getZ() - vec3d.z, 0.0, 0.0, 0.0);
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
        if (!this.getWorld().isClient) {
            ServerWorld serverWorld = (ServerWorld) this.getWorld();
            serverWorld.getPlayers().forEach(player -> {player.sendMessage(Text.literal("onEntityHit"));});
        }
        this.getWorld().createExplosion(this, this.getX(), this.getBodyY(0.0625), this.getZ(), 4.0f, World.ExplosionSourceType.TNT);
        this.hit = true;
        this.discard();
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        if (!this.getWorld().isClient) {
            ServerWorld serverWorld = (ServerWorld) this.getWorld();
            serverWorld.getPlayers().forEach(player -> {player.sendMessage(Text.literal("onBlockHit"));});
        }
        this.getWorld().createExplosion(this, this.getX(), this.getBodyY(0.0625), this.getZ(), 4.0f, World.ExplosionSourceType.TNT);
        this.hit = true;
        this.discard();
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
/*

public class MissileEntity extends ProjectileEntity {

    @Nullable
    private Entity target;
    @Nullable
    private Direction direction;
    private int stepCount;
    private double targetX;
    private double targetY;
    private double targetZ;
    @Nullable
    private UUID targetUuid;
    private Vec3d targetPos;


    public MissileEntity(World world, LivingEntity owner, Entity target, Direction.Axis axis, NbtCompound nbtCompound) {
        this(ModEntities.MISSILE, world);
        this.setNoGravity(false);
        this.setOwner(owner);
        BlockPos blockPos = owner.getBlockPos();
        double d = (double)blockPos.getX() + 0.5;
        double e = (double)blockPos.getY() + 0.5;
        double f = (double)blockPos.getZ() + 0.5;

        this.refreshPositionAndAngles(d, e, f, this.getYaw(), this.getPitch());

        if (nbtCompound.contains("Target")) {
            this.targetUuid = nbtCompound.getUuid("Target");
            this.target = ((ServerWorld)this.getWorld()).getEntity(this.targetUuid);
        } else {
            this.target = null;
        }

        if (this.target != null) {
            this.targetPos = this.target.getPos();
        } else if (nbtCompound.contains("TXD")) {
            this.targetPos = new Vec3d(nbtCompound.getDouble("TXD"), nbtCompound.getDouble("TYD"), nbtCompound.getDouble("TZD"));
        } else {
            this.targetPos = new Vec3d(0.0,0.0,0.0);
        }

        this.targetX = targetPos.getX();
        this.targetY = targetPos.getY();
        this.targetZ = targetPos.getZ();

        this.direction = Direction.UP;
        this.changeTargetDirection(axis);
        owner.sendMessage(Text.literal(this.getPos() + "A"));
    }

    public MissileEntity(EntityType<MissileEntity> missileEntityEntityType, World world) {
        super(missileEntityEntityType, world);
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
        if (this.direction != null) {
            nbt.putInt("Dir", this.direction.getId());
        }
        nbt.putInt("Steps", this.stepCount);
        nbt.putDouble("TXD", this.targetX);
        nbt.putDouble("TYD", this.targetY);
        nbt.putDouble("TZD", this.targetZ);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.stepCount = nbt.getInt("Steps");
        this.targetX = nbt.getDouble("TXD");
        this.targetY = nbt.getDouble("TYD");
        this.targetZ = nbt.getDouble("TZD");
        if (nbt.contains("Dir", NbtElement.NUMBER_TYPE)) {
            this.direction = Direction.byId(nbt.getInt("Dir"));
        }
        if (nbt.containsUuid("Target")) {
            this.targetUuid = nbt.getUuid("Target");
        }
    }

    @Override
    protected void initDataTracker() {
    }

    private void setDirection(@Nullable Direction direction) {
        this.direction = direction;
    }


    private boolean hasDirectViewOfTarget(Vec3d targetPos) {
        HitResult result;
        // Check if there are any obstacles between the entity and the target
        if (targetPos != null) {
            result = this.getWorld().raycast(new RaycastContext(this.getPos(), targetPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));
        } else {
            if (!this.getWorld().isClient) {
                ServerWorld serverWorld = (ServerWorld) this.getWorld();
                serverWorld.getPlayers().forEach(player -> {player.sendMessage(Text.literal("no targetPos"));});
            }
            return false;

        }
        // If the result is null, there are no obstacles, so return true
        return result == null || result.getType() == HitResult.Type.MISS;
    }


    private void changeTargetDirection(@Nullable Direction.Axis axis) {
        BlockPos blockPos;
        double d = 0.5;
        if (this.target == null) {
            blockPos = this.getBlockPos().down();
        } else {
            d = (double)this.target.getHeight() * 0.5; // Target center.
            blockPos = BlockPos.ofFloored(this.target.getX(), this.target.getY() + d, this.target.getZ());
        }

        // Center of the target.
        double e = (double)blockPos.getX() + 0.5;
        double f = (double)blockPos.getY() + d;
        double g = (double)blockPos.getZ() + 0.5;


        Direction direction = null;
        if (!blockPos.isWithinDistance(this.getPos(), 2.0)) {
            BlockPos blockPos2 = this.getBlockPos();
            ArrayList<Direction> list = Lists.newArrayList();
            if (axis != Direction.Axis.X) {
                if (blockPos2.getX() < blockPos.getX() && this.getWorld().isAir(blockPos2.east())) {
                    list.add(Direction.EAST);
                } else if (blockPos2.getX() > blockPos.getX() && this.getWorld().isAir(blockPos2.west())) {
                    list.add(Direction.WEST);
                }
            }
            if (axis != Direction.Axis.Y) {
                if (blockPos2.getY() < blockPos.getY() && this.getWorld().isAir(blockPos2.up())) {
                    list.add(Direction.UP);
                } else if (blockPos2.getY() > blockPos.getY() && this.getWorld().isAir(blockPos2.down())) {
                    list.add(Direction.DOWN);
                }
            }
            if (axis != Direction.Axis.Z) {
                if (blockPos2.getZ() < blockPos.getZ() && this.getWorld().isAir(blockPos2.south())) {
                    list.add(Direction.SOUTH);
                } else if (blockPos2.getZ() > blockPos.getZ() && this.getWorld().isAir(blockPos2.north())) {
                    list.add(Direction.NORTH);
                }
            }
            direction = Direction.random(this.random);
            if (list.isEmpty()) {
                for (int i = 5; !this.getWorld().isAir(blockPos2.offset(direction)) && i > 0; --i) {
                    direction = Direction.random(this.random);
                }
            } else {
                direction = (Direction)list.get(this.random.nextInt(list.size()));
            }
            e = this.getX() + (double)direction.getOffsetX();
            f = this.getY() + (double)direction.getOffsetY();
            g = this.getZ() + (double)direction.getOffsetZ();
        }

        this.setDirection(direction);
        double h = e - this.getX();
        double j = f - this.getY();
        double k = g - this.getZ();
        double l = Math.sqrt(h * h + j * j + k * k);
        if (l == 0.0) {
            this.targetX = 0.0;
            this.targetY = 0.0;
            this.targetZ = 0.0;
        } else {
            this.targetX = h / l * 0.15;
            this.targetY = j / l * 0.15;
            this.targetZ = k / l * 0.15;
        }
        this.velocityDirty = true;
        this.stepCount = 10 + this.random.nextInt(5) * 10;
    }

    @Override
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == Difficulty.PEACEFUL) {
            this.discard();
        }
    }

    private void moveTowardsTargetPos(boolean entity) {
        /*

        this.targetX =  MathHelper.floor(this.targetX) + 0.5;
        this.targetY = MathHelper.floor(this.targetY);
        this.targetZ = MathHelper.floor(this.targetZ) + 0.5;

        if (entity) {
            this.targetY += (double)this.target.getHeight() * 0.5;
        } else {
            this.targetY += 0.5;
        }

        this.targetPos = new Vec3d(this.targetX, this.targetY, this.targetZ);



        Vec3d direction = this.targetPos.subtract(this.getPos()).normalize();

        double velocityMultiplier = 0.15;
        this.setVelocity(direction.multiply(velocityMultiplier));

        this.velocityDirty = true;
    }

    @Override
    public void tick() {
        Vec3d vec3d;
        super.tick();



/*
        // New code
        if (!this.getWorld().isClient) {
            //ServerWorld serverWorld = (ServerWorld) this.getWorld();

            //serverWorld.getPlayers().forEach(player -> {player.sendMessage(Text.literal(this.getX() + " " + this.getY() + " " + this.getZ()));});

            // Retrieves the target from UUID if possible.
            if (this.target == null && this.targetUuid != null) {
                this.target = ((ServerWorld)this.getWorld()).getEntity(this.targetUuid);
                if (this.target == null) {
                    this.targetUuid = null;
                }
            }


            if (this.target == null) {
                serverWorld.getPlayers().forEach(player -> {player.sendMessage(Text.literal("target null move"));});
                moveTowardsTargetPos(false);
            } else if (this.target.isAlive()  && !this.target.isSpectator() && hasDirectViewOfTarget(this.targetPos)) {
                serverWorld.getPlayers().forEach(player -> {player.sendMessage(Text.literal("target move"));});
                moveTowardsTargetPos(true);
            } else {
                serverWorld.getPlayers().forEach(player -> {player.sendMessage(Text.literal("target not found yet"));});
                this.setVelocity(this.getVelocity().add(0.0, -0.04, 0.0));
            }



            this.setVelocity(this.getVelocity().add(0.0, -0.04, 0.0));
        }

        this.setVelocity(this.getVelocity().add(0.0, -0.04, 0.0));




        if (this.getWorld().isClient){
            vec3d = this.getVelocity();
            this.getWorld().addParticle(ParticleTypes.END_ROD, this.getX() - vec3d.x, this.getY() - vec3d.y + 0.15, this.getZ() - vec3d.z, 0.0, 0.0, 0.0);
        }





        if (!this.getWorld().isClient) {
            ServerWorld serverWorld = (ServerWorld) this.getWorld();
            serverWorld.getPlayers().forEach(player -> {player.sendMessage(Text.literal(this.getPos() + "B"));});

            // Retrieves the target from UUID if possible.
            if (this.target == null && this.targetUuid != null) {
                this.target = ((ServerWorld)this.getWorld()).getEntity(this.targetUuid);
                if (this.target == null) {
                    this.targetUuid = null;
                }
            }

            if (this.target != null) {
                this.targetPos = this.target.getPos();
            }

            // If target is valid.
            if (!(this.target == null || !this.target.isAlive() || this.target instanceof PlayerEntity && this.target.isSpectator())) {

                /*
                this.targetX = MathHelper.clamp(this.targetX * 1.025, -1.0, 1.0);
                this.targetY = MathHelper.clamp(this.targetY * 1.025, -1.0, 1.0);
                this.targetZ = MathHelper.clamp(this.targetZ * 1.025, -1.0, 1.0);
                vec3d = this.getVelocity();
                this.setVelocity(vec3d.add((this.targetX - vec3d.x) * 0.2, (this.targetY - vec3d.y) * 0.2, (this.targetZ - vec3d.z) * 0.2));



                if (hasDirectViewOfTarget(this.targetPos)) {
                    // Move directly towards the target
                    moveTowardsTargetPos(true);
                } else {
                    // Follow gravity
                    this.setVelocity(this.getVelocity().add(0.0, -0.04, 0.0));
                }
            } else {
                this.setVelocity(this.getVelocity().add(0.0, -0.04, 0.0));
            }

            HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);

            if (hitResult.getType() != HitResult.Type.MISS) {
                this.onCollision(hitResult);
            }
        }



        this.checkBlockCollision(); // Crash checking

        vec3d = this.getVelocity();
        this.setPosition(this.getX() + vec3d.x, this.getY() + vec3d.y, this.getZ() + vec3d.z);
        ProjectileUtil.setRotationFromVelocity(this, 0.5f);


        if (this.getWorld().isClient) {
            this.getWorld().addParticle(ParticleTypes.END_ROD, this.getX() - vec3d.x, this.getY() - vec3d.y + 0.15, this.getZ() - vec3d.z, 0.0, 0.0, 0.0);

        } else if (this.target != null && !this.target.isRemoved()) {

            if (this.stepCount > 0) {
                --this.stepCount;

                if (this.stepCount == 0) {
                    this.changeTargetDirection(this.direction == null ? null : this.direction.getAxis());
                }
            }

            if (this.direction != null) {
                BlockPos blockPos = this.getBlockPos();
                Direction.Axis axis = this.direction.getAxis();
                if (this.getWorld().isTopSolid(blockPos.offset(this.direction), this)) {
                    this.changeTargetDirection(axis);
                } else {
                    BlockPos blockPos2 = this.target.getBlockPos();
                    if (axis == Direction.Axis.X && blockPos.getX() == blockPos2.getX() || axis == Direction.Axis.Z && blockPos.getZ() == blockPos2.getZ() || axis == Direction.Axis.Y && blockPos.getY() == blockPos2.getY()) {
                        this.changeTargetDirection(axis);
                    }
                }
            }


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
        if (!this.getWorld().isClient) {
            ServerWorld serverWorld = (ServerWorld) this.getWorld();
            serverWorld.getPlayers().forEach(player -> {player.sendMessage(Text.literal("onEntityHit"));});
        }
        this.getWorld().createExplosion(this, this.getX(), this.getBodyY(0.0625), this.getZ(), 4.0f, World.ExplosionSourceType.TNT);
        this.discard();
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        if (!this.getWorld().isClient) {
            ServerWorld serverWorld = (ServerWorld) this.getWorld();
            serverWorld.getPlayers().forEach(player -> {player.sendMessage(Text.literal("onBlockHit"));});
        }
        this.getWorld().createExplosion(this, this.getX(), this.getBodyY(0.0625), this.getZ(), 4.0f, World.ExplosionSourceType.TNT);
        this.discard();
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
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        double d = packet.getVelocityX();
        double e = packet.getVelocityY();
        double f = packet.getVelocityZ();
        this.setVelocity(d, e, f);
    }
}
*/
