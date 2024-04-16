package net.alvisssss.advancedmissile.item.custom;


import net.alvisssss.advancedmissile.entity.custom.MissileEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class LauncherItem extends Item {

    public LauncherItem(Settings settings) {
        super(settings);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity playerEntity)) {
            return;
        }
        int i = this.getMaxUseTime(stack) - remainingUseTicks;
        if (i < 10) {
            return;
        }
        if (!world.isClient) {
            // Damage trident
            stack.damage(1, playerEntity, p -> p.sendToolBreakStatus(user.getActiveHand()));
            if (true) {
                // Change it for missile itself?
                MissileEntity missileEntity = new MissileEntity(world, (LivingEntity)playerEntity, stack);
                // Unknown method: Set whose velocity? Player? Trident?
                //missileEntity.setVelocity(playerEntity, playerEntity.getPitch(), playerEntity.getYaw(), 0.0f, 2.5f + (float)j * 0.5f, 1.0f);
                // Creative pick up creative trident
                if (playerEntity.getAbilities().creativeMode) {
                    missileEntity.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
                }
                world.spawnEntity(missileEntity);
                //world.playSoundFromEntity(null, missileEntity, SoundEvents.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
        }
    }
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        // Trident specific (If item has durability left)
        if (itemStack.getDamage() >= itemStack.getMaxDamage() - 1) {
            return TypedActionResult.fail(itemStack);
        }
        // Trident specific (If trident has riptide and user not touching water or rain)
        if (EnchantmentHelper.getRiptide(itemStack) > 0 && !user.isTouchingWaterOrRain()) {
            return TypedActionResult.fail(itemStack);
        }
        user.setCurrentHand(hand);
        return TypedActionResult.success(itemStack, false);
    }
}
