package net.alvisssss.advancedmissile;

import net.alvisssss.advancedmissile.block.ModBlocks;
import net.alvisssss.advancedmissile.item.ModItems;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdvancedMissile implements ModInitializer {
	public static final String MOD_ID = "advancedmissile";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		ModItems.registerModItems();
		ModBlocks.registerModBlocks();
	}
}