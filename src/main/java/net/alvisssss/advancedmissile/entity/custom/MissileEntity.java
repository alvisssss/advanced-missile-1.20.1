package net.alvisssss.advancedmissile.entity.custom;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class MissileEntity extends TridentEntity {

    // Ownership still on owner?
    public MissileEntity(World world, LivingEntity owner, ItemStack stack) {
        super(world, owner, stack);
    }

    private boolean isTargetAlive() {
        Entity entity = this.getOwner();
        if (entity == null || !entity.isAlive()) {
            return false;
        }
        return !(entity instanceof ServerPlayerEntity) || !entity.isSpectator();
    }
    @Override
    public void tick() {

        Entity entity = this.getOwner();
        byte i = this.dataTracker.get(DataTracker.registerData(MissileEntity.class, TrackedDataHandlerRegistry.BYTE));
        if (entity != null) {
            if (!this.isTargetAlive()) {
                if (!this.getWorld().isClient && this.pickupType == PersistentProjectileEntity.PickupPermission.ALLOWED) {
                    this.dropStack(this.asItemStack(), 0.1f);
                }
                this.discard();
            } else {
                this.setNoClip(true);
                Vec3d vec3d = entity.getEyePos().subtract(this.getPos());
                this.setPos(this.getX(), this.getY() + vec3d.y * 0.015 * (double)i, this.getZ());
                if (this.getWorld().isClient) {
                    this.lastRenderY = this.getY();
                }
                double d = 0.05 * (double)i;
                this.setVelocity(this.getVelocity().multiply(0.95).add(vec3d.normalize().multiply(d)));
            }
        }
        super.tick();
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();
        float f = 8.0f;
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            //f += EnchantmentHelper.getAttackDamage(this.tridentStack, livingEntity.getGroup());
        }
        Entity entity2 = this.getOwner();
        DamageSource damageSource = this.getDamageSources().trident(this, entity2 == null ? this : entity2);
        // this.dealtDamage = true;
        //SoundEvent soundEvent = SoundEvents.ITEM_TRIDENT_HIT;
        if (entity.damage(damageSource, f)) {
            if (entity.getType() == EntityType.ENDERMAN) {
                return;
            }
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity2 = (LivingEntity)entity;
                if (entity2 instanceof LivingEntity) {
                    EnchantmentHelper.onUserDamaged(livingEntity2, entity2);
                    EnchantmentHelper.onTargetDamaged((LivingEntity)entity2, livingEntity2);
                }
                this.onHit(livingEntity2);
            }
        }
        this.setVelocity(this.getVelocity().multiply(-0.01, -0.1, -0.01));

        //this.playSound(soundEvent, g, 1.0f);
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        this.discard();
        // Explosion??????
    }
}
