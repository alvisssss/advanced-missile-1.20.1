package net.alvisssss.advancedmissile.block.entity;

import net.alvisssss.advancedmissile.item.ModItems;
import net.alvisssss.advancedmissile.screen.UpgradingScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class UpgradingFactoryBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(5, ItemStack.EMPTY);

    private static final int MISSILE_SLOT = 0;
    private static final int FUEL_SLOT = 1;
    private static final int WARHEAD_SLOT = 2;
    private static final int OUTPUT_SLOT = 3;
    private static final int LOCATOR_SLOT = 4;
    private static boolean outputCreated = false;

    ItemStack fuelStack;
    ItemStack warheadStack;


    public UpgradingFactoryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.UPGRADING_FACTORY_BLOCK_ENTITY, pos, state);

    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Upgrading Factory");
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new UpgradingScreenHandler(syncId, playerInventory, this);
    }

    public void tick(World world, BlockPos pos, BlockState state) {
        if (world.isClient()) {
            return;
        }
        if (hasCorrectInputs()) {
            if (isOutputSlotEmpty() || this.getStack(OUTPUT_SLOT).getItem() == ModItems.MISSILE) {
                if (outputCreated && isOutputSlotEmpty()) {
                    this.removeInputs();
                } else {
                    this.craftItem();
                }
            }
            markDirty(world, pos, state);
        } else {
            removeOutput();
            markDirty(world, pos, state);
        }
    }

    private void removeInputs() {
        this.removeStack(MISSILE_SLOT,1);
        this.removeStack(FUEL_SLOT, this.getStack(FUEL_SLOT).getCount());
        this.removeStack(WARHEAD_SLOT, this.getStack(WARHEAD_SLOT).getCount());

        outputCreated = false;
    }

    private void removeOutput() {
        this.removeStack(OUTPUT_SLOT,this.getStack(OUTPUT_SLOT).getCount());
        outputCreated = false;
    }




    private void craftItem() {

        ItemStack result = this.getStack(MISSILE_SLOT).copy();

        NbtCompound resultNbt = result.getOrCreateNbt();

        if (this.getStack(LOCATOR_SLOT).hasNbt()) {
            resultNbt.copyFrom(this.getStack(LOCATOR_SLOT).getNbt());
        }

        if (this.getStack(FUEL_SLOT).getItem() == Items.GUNPOWDER) {
            if (resultNbt.contains("fuel_count", NbtElement.INT_TYPE)) {
                resultNbt.putInt("fuel_count", resultNbt.getInt("fuel_count") + this.getStack(FUEL_SLOT).getCount());
            } else {
                resultNbt.putInt("fuel_count", this.getStack(FUEL_SLOT).getCount());
            }
        }

        if (this.getStack(WARHEAD_SLOT).getItem() == Items.TNT) {
            if (resultNbt.contains("tnt_count", NbtElement.INT_TYPE)) {
                resultNbt.putInt("tnt_count", resultNbt.getInt("tnt_count") + this.getStack(WARHEAD_SLOT).getCount());
            } else {
                resultNbt.putInt("tnt_count", this.getStack(WARHEAD_SLOT).getCount());
            }
        }

        this.fuelStack = this.getStack(FUEL_SLOT).copy();
        this.warheadStack = this.getStack(MISSILE_SLOT).copy();

        this.setStack(OUTPUT_SLOT, result);

        outputCreated = true;
    }

    private boolean hasCorrectInputs() {
        return this.getStack(MISSILE_SLOT).getItem() == ModItems.MISSILE
                && this.getStack(LOCATOR_SLOT).getItem() == ModItems.LOCATOR
                && (this.getStack(FUEL_SLOT).getItem() == Items.GUNPOWDER || this.getStack(FUEL_SLOT).isEmpty())
                && (this.getStack(WARHEAD_SLOT).getItem() == Items.TNT || this.getStack(WARHEAD_SLOT).isEmpty());
    }

    private boolean isOutputSlotEmpty() {
        return this.getStack(OUTPUT_SLOT).isEmpty();
    }
}