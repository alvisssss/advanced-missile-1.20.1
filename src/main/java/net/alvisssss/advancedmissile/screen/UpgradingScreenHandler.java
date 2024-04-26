package net.alvisssss.advancedmissile.screen;

import net.alvisssss.advancedmissile.block.entity.UpgradingFactoryBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.ForgingSlotsManager;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.OptionalInt;

public class UpgradingScreenHandler
        extends ScreenHandler {
    private final Inventory inventory;
    public final UpgradingFactoryBlockEntity blockEntity;

    public UpgradingScreenHandler(int syncId, PlayerInventory inventory, PacketByteBuf buf) {
        this(syncId, inventory, inventory.player.getWorld().getBlockEntity(buf.readBlockPos()));
    }

    public UpgradingScreenHandler(int syncId, PlayerInventory playerInventory,
                                  BlockEntity blockEntity) {
        super(ModScreenHandlers.UPGRADING_SCREEN_HANDLER, syncId);
        checkSize(((Inventory) blockEntity), 5);
        this.inventory = ((Inventory) blockEntity);
        inventory.onOpen(playerInventory.player);
        this.blockEntity = ((UpgradingFactoryBlockEntity) blockEntity);

        this.addSlot(new Slot(inventory, 0, 8, 48)); // Missile
        this.addSlot(new Slot(inventory, 1, 26, 48)); // Fuel
        this.addSlot(new Slot(inventory, 2, 44, 48)); // Warhead
        this.addSlot(new Slot(inventory, 3, 98, 48)); // Output
        this.addSlot(new Slot(inventory, 4, 44, 30)); // Locator


        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

    }


    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.size()) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return newStack;
    }



    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}

