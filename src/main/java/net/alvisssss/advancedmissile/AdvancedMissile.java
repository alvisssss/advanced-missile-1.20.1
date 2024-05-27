package net.alvisssss.advancedmissile;

import net.alvisssss.advancedmissile.block.ModBlocks;
import net.alvisssss.advancedmissile.entity.ModEntities;
import net.alvisssss.advancedmissile.item.ModItemGroups;
import net.alvisssss.advancedmissile.item.ModItems;
import net.alvisssss.advancedmissile.keybind.CommandLaunchUnitHandler;
import net.alvisssss.advancedmissile.network.MissileFirePacket;
import net.alvisssss.advancedmissile.network.MissileReloadPacket;
import net.alvisssss.advancedmissile.recipe.ModRecipes;
import net.alvisssss.advancedmissile.screen.ModScreenHandlers;
import net.alvisssss.advancedmissile.sound.ModSounds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdvancedMissile implements ModInitializer {
	public static final String MOD_ID = "advancedmissile";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


	@Override
	public void onInitialize() {

		ServerPlayNetworking.registerGlobalReceiver(MissileFirePacket.ID, (server, player, handler, buf, responseSender) -> {
			server.execute(() -> {
				CommandLaunchUnitHandler.handleMissileFire(player);
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(MissileReloadPacket.ID, (server, player, handler, buf, responseSender) -> {
			server.execute(() -> {
				CommandLaunchUnitHandler.handleMissileReload(player);
			});
		});

		ModEntities.registerModEntities();

		ModItemGroups.registerItemGroups();
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();

		ModScreenHandlers.registerScreenHandlers();

		ModRecipes.registerRecipes();

		ModSounds.registerSounds();

	}
}