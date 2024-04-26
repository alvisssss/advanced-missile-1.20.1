package net.alvisssss.advancedmissile.item.custom;

import net.alvisssss.advancedmissile.item.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
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

        NbtCompound nbt = user.getStackInHand(hand).getOrCreateNbt();
        nbt.putInt("id", entity.getId());
        nbt.putUuid("Target", entity.getUuid());
        nbt.putString("name", String.valueOf(entity.getDisplayName().getString()));
        nbt.putDouble("TXD", entity.getX());
        nbt.putDouble("TYD", entity.getY());
        nbt.putDouble("TZD", entity.getZ());

        //test messages
        user.sendMessage(Text.literal("Name: " + entity.getDisplayName().getString()));
        user.sendMessage(Text.literal("Target XYZ : " +
                entity.getX() + " " + entity.getY() + " " + entity.getZ()), false);

        return ActionResult.SUCCESS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        ItemStack itemStack = user.getStackInHand(hand);
        ItemStack itemStack1 = user.getStackInHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND);

        if (itemStack1.isOf(ModItems.LAUNCHER)) {
            itemStack1.getOrCreateNbt();
        }

        Vec3d position;
        int rayTraceDistance = 2000;
        HitResult hitResult = user.raycast(rayTraceDistance, 0, false);
        NbtCompound nbt = itemStack.getOrCreateNbt();

        if (hitResult.getType() == HitResult.Type.MISS) {
            user.sendMessage(Text.literal("Unable to obtain coordinates"), false);
            return TypedActionResult.fail(itemStack);
        } else {

            BlockHitResult blockHitResult = ((BlockHitResult) hitResult);
            BlockState blockState = world.getBlockState(blockHitResult.getBlockPos());
            position = blockHitResult.getPos();
            nbt.putString("name", blockState.getBlock().getName().getString());
            nbt.putDouble("TXD", position.getX());
            nbt.putDouble("TYD", position.getY());
            nbt.putDouble("TZD", position.getZ());

            // Test messages
            user.sendMessage(Text.literal("Name: " + blockState.getBlock().getName().getString()), false);
            user.sendMessage(Text.literal("Target XYZ : " +
                    position.getX() + " " + position.getY() + " " + position.getZ()), false);
        }
        return TypedActionResult.success(itemStack);
    }
}
