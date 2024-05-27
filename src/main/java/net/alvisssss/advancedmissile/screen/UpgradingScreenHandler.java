package net.alvisssss.advancedmissile.screen;

import net.alvisssss.advancedmissile.block.ModBlocks;
import net.alvisssss.advancedmissile.item.ModItems;
import net.alvisssss.advancedmissile.recipe.UpgradingRecipe;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.ForgingSlotsManager;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UpgradingScreenHandler
        extends ForgingScreenHandler {

    private final World world;
    @Nullable
    private RecipeEntry<UpgradingRecipe> currentRecipe;

    public UpgradingScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        super(ModScreenHandlers.UPGRADING_SCREEN_HANDLER, syncId, playerInventory, ScreenHandlerContext.EMPTY);
        this.world = playerInventory.player.getWorld();
    }
    // Checks if the available recipe is matching the input inventory.
    @Override
    protected boolean canTakeOutput(PlayerEntity player, boolean present) {
        return this.currentRecipe != null && this.currentRecipe.value().matches(this.input, this.world);
    }
    // Updates the world and player stats. Decreases input items.
    @Override
    protected void onTakeOutput(PlayerEntity player, ItemStack stack) {
        stack.onCraft(player.getWorld(), player, stack.getCount());
        this.decrementStack(0, 1);
        this.decrementStack(1, this.input.getStack(1).getCount());
        this.decrementStack(2, this.input.getStack(2).getCount());
    }
    // Decreases the items in the input by a specific amount.
    private void decrementStack(int slot, int count) {
        ItemStack itemStack = this.input.getStack(slot);
        if (!itemStack.isEmpty()) {
            itemStack.decrement(count);
            this.input.setStack(slot, itemStack);
        }
    }
    @Override
    protected boolean canUse(BlockState state) {
        return state.isOf(ModBlocks.UPGRADING_FACTORY);
    }

    @Override
    public void updateResult() {
        // Get all recipes that can be used and matching the input.
        List<RecipeEntry<UpgradingRecipe>> list = this.world.getRecipeManager().getAllMatches(UpgradingRecipe.Type.INSTANCE, this.input, this.world);
        if (list.isEmpty()) {
            this.output.setStack(0, ItemStack.EMPTY);
        } else {
            // Uses the first recipe available by default.
            RecipeEntry<UpgradingRecipe> recipeEntry = list.get(0);
            // Crafts output.
            ItemStack itemStack = recipeEntry.value().craft(this.input, this.world.getRegistryManager());
            this.currentRecipe = recipeEntry;
            this.output.setStack(0, itemStack);

        }
    }
    @Override
    protected ForgingSlotsManager getForgingSlotsManager() {
        return ForgingSlotsManager.create() // Slot (Input/Output) locations and what can be in the slots.
                .input(0, 8, 48, stack -> stack.getItem() == ModItems.MISSILE) // Missile
                .input(1, 26, 48, stack -> stack.getItem() == Items.GUNPOWDER) // Fuel
                .input(2, 44, 48, stack -> stack.getItem() == Items.TNT) // Warhead
                .input(3, 44, 30, stack -> stack.getItem() == ModItems.LOCATOR) // Locator
                .output(4, 98, 48) // Missile output

                //.input(5, 134, 30, stack -> stack.getItem() == ModItems.LAUNCH_TUBE) // Launch Tube
                //.input(6, 134, 12, stack -> stack.getItem() == ModItems.CLU) // CLU
                //.output(7, 80,30) // MANPAD output
                .build();
    }
    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.dropInventory(player, this.input);
    }
}
