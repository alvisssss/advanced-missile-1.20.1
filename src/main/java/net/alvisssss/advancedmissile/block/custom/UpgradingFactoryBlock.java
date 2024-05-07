package net.alvisssss.advancedmissile.block.custom;

import io.netty.buffer.Unpooled;
import net.alvisssss.advancedmissile.screen.UpgradingScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UpgradingFactoryBlock extends Block {
    public UpgradingFactoryBlock(Settings settings) {
        super(settings);
    } // Block properties i.e. sound, blast resistance etc.

    // The Extended Screen Handler Factory is used as the GUI and the logic behind is custom.
    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return new ExtendedScreenHandlerFactory() {
            @Override
            public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) { // Null.
            }
            @Override
            public Text getDisplayName() {
                return Text.translatable("container.upgrade");
            }

            // Initializes the UpgradingScreenHandler, also known as the GUI.
            @Override
            public @NotNull ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                return new UpgradingScreenHandler(syncId, playerInventory,new PacketByteBuf(Unpooled.buffer()) );
            }
        };
    }
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) { // Ensures any action is performed by the server.
            return ActionResult.SUCCESS;
        }
        player.openHandledScreen(state.createScreenHandlerFactory(world, pos)); // Opens the GUI for the player.
        return ActionResult.SUCCESS;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        tooltip.add(Text.literal("Block for upgrading missile and adjusting settings.")); // Display text when hovering over an item.
    }
}
