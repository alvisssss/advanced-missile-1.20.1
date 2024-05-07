package net.alvisssss.advancedmissile.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;

import java.util.List;

public class UpgradingRecipe implements Recipe<Inventory> {

    private final List<Ingredient> recipeItems;
    private final ItemStack output;

    public UpgradingRecipe(List<Ingredient> ingredients, ItemStack itemStack) {

        this.recipeItems = ingredients;
        this.output = itemStack;
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        // True if missile slot is not empty and any other input slots are not empty
        return this.recipeItems.get(0).test(inventory.getStack(0))
                && (!inventory.getStack(1).isEmpty() || !inventory.getStack(2).isEmpty() || !inventory.getStack(3).isEmpty());
    }

    @Override
    public ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager) {
        ItemStack itemStack = this.output.copy();

        // NBT data transfer
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack ingredientStack = inventory.getStack(i);
            if (!ingredientStack.isEmpty() && ingredientStack.hasNbt()) {
                itemStack.getOrCreateNbt().copyFrom(ingredientStack.getNbt());
            }
        }

        // Additional NBT data depending on the input, ie fuel and warhead.
        if (!inventory.getStack(1).isEmpty()) {
            itemStack.getOrCreateNbt().putInt("fuel_count", inventory.getStack(1).getCount() + itemStack.getOrCreateNbt().getInt("fuel_count"));
        }
        if (!inventory.getStack(2).isEmpty()) {
            itemStack.getOrCreateNbt().putInt("tnt_count", inventory.getStack(2).getCount() + itemStack.getOrCreateNbt().getInt("tnt_count"));
        }
        return itemStack;
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResult(DynamicRegistryManager registryManager) {
        return this.output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }
    public static class Type implements RecipeType<UpgradingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "upgrading";
    }
    @Override
    public RecipeType<?> getType() {return Type.INSTANCE;}

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        DefaultedList<Ingredient> list = DefaultedList.ofSize(this.recipeItems.size());
        list.addAll(recipeItems);
        return list;
    }

    public static class Serializer
            implements RecipeSerializer<UpgradingRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final String ID = "upgrading";

        // Codec finds any json file with type "upgrading" specified.
        // Checks field called "ingredients" and puts all items into a list in order.
        // Checks field called "output" for the output item.
        public static final Codec<UpgradingRecipe> CODEC = RecordCodecBuilder.create(in -> in.group(
                validateAmount(Ingredient.DISALLOW_EMPTY_CODEC, 9).fieldOf("ingredients").forGetter(UpgradingRecipe::getIngredients),
                RecipeCodecs.CRAFTING_RESULT.fieldOf("output").forGetter(r -> r.output)
        ).apply(in, UpgradingRecipe::new));


        private static Codec<List<Ingredient>> validateAmount(Codec<Ingredient> delegate, int max) {
            return Codecs.validate(Codecs.validate(
                    delegate.listOf(), list -> list.size() > max ? DataResult.error(() -> "Recipe has too many ingredients!") : DataResult.success(list)
            ), list -> list.isEmpty() ? DataResult.error(() -> "Recipe has no ingredients!") : DataResult.success(list));
        }

        @Override
        public Codec<UpgradingRecipe> codec() {
            return CODEC;
        }

        @Override
        public UpgradingRecipe read(PacketByteBuf packetByteBuf) {
            DefaultedList<Ingredient> inputs = DefaultedList.ofSize(packetByteBuf.readInt(), Ingredient.EMPTY);

            for(int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromPacket(packetByteBuf));
            }
            ItemStack output = packetByteBuf.readItemStack();
            return new UpgradingRecipe(inputs, output);
        }

        @Override
        public void write(PacketByteBuf buf, UpgradingRecipe recipe) {
            buf.writeInt(recipe.getIngredients().size());

            for (Ingredient ingredient : recipe.getIngredients()) {
                ingredient.write(buf);
            }

            buf.writeItemStack(recipe.output);
        }
    }

}
