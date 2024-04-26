package net.alvisssss.advancedmissile.block.custom;

import net.alvisssss.advancedmissile.block.entity.ModBlockEntities;
import net.alvisssss.advancedmissile.block.entity.UpgradingFactoryBlockEntity;
import net.alvisssss.advancedmissile.item.ModItems;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TurretBlock extends Block {

    public TurretBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.getItem() == ModItems.MISSILE) {
            NbtCompound nbt = itemStack.getNbt();


            return ActionResult.SUCCESS;
        }
        return ActionResult.FAIL;
    }
}
