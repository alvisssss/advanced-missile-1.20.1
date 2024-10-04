package net.alvisssss.advancedmissile.util;

import net.alvisssss.advancedmissile.entity.custom.MissileEntity;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class MissileDispenserBehaviour extends ItemDispenserBehavior {

    @Override
    protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        World world = pointer.world();
        Direction direction = pointer.state().get(DispenserBlock.FACING);
        Vec3d position = pointer.centerPos();

        double offsetX = 0.5 * direction.getOffsetX();
        double offsetY = 0.5 * direction.getOffsetY();
        double offsetZ = 0.5 * direction.getOffsetZ();
        position = position.add(offsetX, offsetY, offsetZ);

        float missileSpeed = calculateMissileSpeed(world.getReceivedRedstonePower(pointer.blockEntity().getPos()));

        if (!world.isClient()) {
            NbtCompound nbt = stack.getOrCreateNbt();
            nbt.putFloat("speed", missileSpeed);

            MissileEntity missile = new MissileEntity(world, stack.getNbt());

            world.spawnEntity(missile);
            missile.setVelocity(direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ(), missileSpeed, 0f);


            missile.refreshPositionAndAngles(position.getX(), position.getY(), position.getZ(), missile.getYaw(), missile.getPitch());

            stack.decrement(1);
        }


        return stack;
    }

    private float calculateMissileSpeed(int powerLevel) {
        float baseSpeed = 0.75f;
        float maxSpeed = 3.5f;
        float speedRange = maxSpeed - baseSpeed;
        return baseSpeed + (speedRange * ((float) (powerLevel-1) /14));
    }
}
