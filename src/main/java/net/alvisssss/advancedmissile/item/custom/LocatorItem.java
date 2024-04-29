package net.alvisssss.advancedmissile.item.custom;

import net.alvisssss.advancedmissile.item.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class LocatorItem extends Item {

    public LocatorItem(Settings settings) {
        super(settings.maxCount(1));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!context.getWorld().isClient()) {
            BlockState blockState = context.getWorld().getBlockState(context.getBlockPos());
            if (Objects.equals(blockState.getBlock().getName().getString(), "Turret Block")) {

            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {

        if (!user.getWorld().isClient()) {
            user.getStackInHand(hand).setNbt(new NbtCompound());
            NbtCompound nbt = user.getStackInHand(hand).getOrCreateNbt();

            nbt.putBoolean("isBlock", false);
            nbt.putInt("id", entity.getId());
            nbt.putUuid("Target", entity.getUuid());
            nbt.putString("name", String.valueOf(entity.getDisplayName().getString()));
            nbt.putDouble("TXD", entity.getX());
            nbt.putDouble("TYD", entity.getY());
            nbt.putDouble("TZD", entity.getZ());

            //test messages
            user.sendMessage(Text.literal("Name: " + entity.getDisplayName().getString()));
            user.sendMessage(Text.literal("Target XYZ : " + ((int) entity.getX()) + " " + ((int) entity.getY()) + " " + ((int) entity.getZ())), false);
        }
        
        return ActionResult.SUCCESS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        ItemStack itemStack = user.getStackInHand(hand);

        if (!world.isClient()) {
            Vec3d position;
            int rayTraceDistance = 500;
            HitResult hitResult = user.raycast(rayTraceDistance, 0, false);

            if (!user.getItemCooldownManager().isCoolingDown(this)) {

                user.getItemCooldownManager().set(this, 20);

                if (hitResult.getType() == HitResult.Type.MISS) {
                    user.sendMessage(Text.literal("Unable to obtain coordinates"), false);
                    return TypedActionResult.fail(itemStack);
                } else {

                    BlockHitResult blockHitResult = ((BlockHitResult) hitResult);
                    BlockState blockState = world.getBlockState(blockHitResult.getBlockPos());
                    position = blockHitResult.getPos();

                    itemStack.setNbt(new NbtCompound());
                    NbtCompound nbt = itemStack.getOrCreateNbt();

                    nbt.putBoolean("isBlock", true);
                    nbt.putString("name", blockState.getBlock().getName().getString());
                    nbt.putDouble("TXD", position.getX());
                    nbt.putDouble("TYD", position.getY());
                    nbt.putDouble("TZD", position.getZ());

                    // Test messages
                    user.sendMessage(Text.literal("Name: " + blockState.getBlock().getName().getString()), false);
                    user.sendMessage(Text.literal("Target XYZ : " +
                            ((int) position.getX()) + " " + ((int) position.getY()) + " " + ((int) position.getZ())), false);
                }
            }
        }

        return TypedActionResult.success(itemStack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.literal("Obtains data of target with right-click."));
    }
}
