package net.alvisssss.advancedmissile;

import net.alvisssss.advancedmissile.block.ModBlocks;
import net.alvisssss.advancedmissile.entity.ModEntities;
import net.alvisssss.advancedmissile.item.ModItemGroups;
import net.alvisssss.advancedmissile.item.ModItems;
import net.alvisssss.advancedmissile.recipe.ModRecipes;
import net.alvisssss.advancedmissile.screen.ModScreenHandlers;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdvancedMissile implements ModInitializer {
	public static final String MOD_ID = "advancedmissile";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		ModEntities.registerModEntities();

		ModItemGroups.registerItemGroups();
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();

		ModScreenHandlers.registerScreenHandlers();

		ModRecipes.registerRecipes();

	}
}