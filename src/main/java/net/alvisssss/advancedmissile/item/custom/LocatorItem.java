package net.alvisssss.advancedmissile.item.custom;

import net.alvisssss.advancedmissile.item.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class LocatorItem extends Item {

    public LocatorItem(Settings settings) {
        super(settings.maxCount(1));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!context.getWorld().isClient()) {
            Vec3d position = context.getBlockPos().toCenterPos();
            BlockState blockState = context.getWorld().getBlockState(context.getBlockPos());

            ItemStack itemStack = context.getStack();
            itemStack.setNbt(new NbtCompound());
            NbtCompound nbt = itemStack.getOrCreateNbt();

            nbt.putBoolean("isBlock", true);
            nbt.putString("name", blockState.getBlock().getName().getString());
            nbt.putDouble("TXD", position.getX());
            nbt.putDouble("TYD", position.getY());
            nbt.putDouble("TZD", position.getZ());

            Objects.requireNonNull(context.getPlayer()).sendMessage(Text.literal("Name: " + blockState.getBlock().getName().getString()), false);
            Objects.requireNonNull(context.getPlayer()).sendMessage(Text.literal("Target XYZ : " + ((int) position.getX()) + " " + ((int) position.getY()) + " " + ((int) position.getZ())), false);
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

            user.sendMessage(Text.literal("Name: " + entity.getDisplayName().getString()));
            user.sendMessage(Text.literal("Target XYZ : " + ((int) entity.getX()) + " " + ((int) entity.getY()) + " " + ((int) entity.getZ())), false);


        }
        
        return ActionResult.SUCCESS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        ItemStack itemStack = user.getStackInHand(hand);

        if (!world.isClient()) {

            int maxDistance = 500;
            Vec3d vec3d = user.getCameraPosVec(0.0f);
            Vec3d vec3d2 = vec3d.add(user.getRotationVec(0.0f).multiply(100));

            double boxSize = 1.0;

            Vec3d minCorner = new Vec3d(
                    Math.min(vec3d.x, vec3d2.x) - boxSize,
                    Math.min(vec3d.y, vec3d2.y) - boxSize,
                    Math.min(vec3d.z, vec3d2.z) - boxSize
            );
            Vec3d maxCorner = new Vec3d(
                    Math.max(vec3d.x, vec3d2.x) + boxSize,
                    Math.max(vec3d.y, vec3d2.y) + boxSize,
                    Math.max(vec3d.z, vec3d2.z) + boxSize
            );

            EntityHitResult entityHitResult = ProjectileUtil.raycast(user, vec3d, vec3d2, new Box(minCorner, maxCorner), entity -> true, maxDistance);

            if (!user.getItemCooldownManager().isCoolingDown(this)) {

                user.getItemCooldownManager().set(this, 20);

                if (entityHitResult != null) {
                    user.getStackInHand(hand).setNbt(new NbtCompound());
                    NbtCompound nbt = user.getStackInHand(hand).getOrCreateNbt();
                    LivingEntity entity = (LivingEntity) entityHitResult.getEntity();

                    nbt.putBoolean("isBlock", false);
                    nbt.putInt("id", entity.getId());
                    nbt.putUuid("Target", entity.getUuid());
                    nbt.putString("name", String.valueOf(entity.getDisplayName().getString()));
                    nbt.putDouble("TXD", entity.getX());
                    nbt.putDouble("TYD", entity.getY());
                    nbt.putDouble("TZD", entity.getZ());

                    user.sendMessage(Text.literal("Name: " + entity.getDisplayName().getString()));
                    user.sendMessage(Text.literal("Target XYZ : " + ((int) entity.getX()) + " " + ((int) entity.getY()) + " " + ((int) entity.getZ())), false);


                } else {

                    BlockHitResult blockHitResult = world.raycast(new RaycastContext(vec3d, vec3d2, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, user));

                    if (blockHitResult == null) {
                        user.sendMessage(Text.literal("Unable to obtain coordinates"), false);
                        return TypedActionResult.fail(itemStack);

                    } else {
                        BlockState blockState = world.getBlockState(blockHitResult.getBlockPos());
                        BlockPos position = blockHitResult.getBlockPos();

                        itemStack.setNbt(new NbtCompound());
                        NbtCompound nbt = itemStack.getOrCreateNbt();

                        nbt.putBoolean("isBlock", true);
                        nbt.putString("name", blockState.getBlock().getName().getString());
                        nbt.putDouble("TXD", position.getX() + 0.5);
                        nbt.putDouble("TYD", position.getY() + 0.5);
                        nbt.putDouble("TZD", position.getZ() + 0.5);

                        user.sendMessage(Text.literal("Name: " + blockState.getBlock().getName().getString()), false);
                        user.sendMessage(Text.literal("Target XYZ : " + position.getX() + " " + position.getY() + " " + position.getZ()), false);
                    }
                }
            }
        }

        return TypedActionResult.success(itemStack);
    }
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.literal("Obtains target's data by right-clicking."));
        if (stack.hasNbt()) {
            if (stack.getNbt().contains("name")) {
                tooltip.add(Text.literal("Name: " + stack.getNbt().getString("name")));
                tooltip.add(Text.literal("Target XYZ: " + ((int) stack.getNbt().getDouble("TXD")) + " " + (((int) stack.getNbt().getDouble("TYD"))) + " " + ((int) stack.getNbt().getDouble("TZD"))));
            }
        }
    }
}
