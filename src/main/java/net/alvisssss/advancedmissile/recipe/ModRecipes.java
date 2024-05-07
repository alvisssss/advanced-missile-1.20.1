package net.alvisssss.advancedmissile.recipe;

import net.alvisssss.advancedmissile.AdvancedMissile;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModRecipes {
    public static void registerRecipes() {

        Registry.register(Registries.RECIPE_SERIALIZER, new Identifier(AdvancedMissile.MOD_ID, UpgradingRecipe.Serializer.ID),
                UpgradingRecipe.Serializer.INSTANCE);

        Registry.register(Registries.RECIPE_TYPE, new Identifier(AdvancedMissile.MOD_ID, UpgradingRecipe.Type.ID),
                UpgradingRecipe.Type.INSTANCE);
    }
}
