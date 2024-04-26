package net.alvisssss.advancedmissile;

import net.alvisssss.advancedmissile.block.ModBlocks;
import net.alvisssss.advancedmissile.block.entity.ModBlockEntities;
import net.alvisssss.advancedmissile.entity.ModEntities;
import net.alvisssss.advancedmissile.entity.custom.MissileEntity;
import net.alvisssss.advancedmissile.item.ModItemGroups;
import net.alvisssss.advancedmissile.item.ModItems;
import net.alvisssss.advancedmissile.recipe.ModRecipes;
import net.alvisssss.advancedmissile.screen.ModScreenHandlers;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdvancedMissile implements ModInitializer {
	public static final String MOD_ID = "advancedmissile";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		ModBlockEntities.registerBlockEntities();
		ModEntities.registerModEntities();

		ModItemGroups.registerItemGroups();
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();

		ModScreenHandlers.registerScreenHandlers();

		ModRecipes.registerRecipes();

	}
}