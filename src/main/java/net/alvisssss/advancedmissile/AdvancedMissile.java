package net.alvisssss.advancedmissile;

import net.alvisssss.advancedmissile.block.ModBlocks;
import net.alvisssss.advancedmissile.block.entity.ModBlockEntities;
import net.alvisssss.advancedmissile.entity.ModEntities;
import net.alvisssss.advancedmissile.entity.custom.MissileEntity;
import net.alvisssss.advancedmissile.item.ModItemGroups;
import net.alvisssss.advancedmissile.item.ModItems;
import net.alvisssss.advancedmissile.keybind.CommandLaunchUnitHandler;
import net.alvisssss.advancedmissile.network.MissileFirePacket;
import net.alvisssss.advancedmissile.network.MissileModePacket;
import net.alvisssss.advancedmissile.network.MissileReloadPacket;
import net.alvisssss.advancedmissile.recipe.ModRecipes;
import net.alvisssss.advancedmissile.screen.ModScreenHandlers;
import net.alvisssss.advancedmissile.sound.ModSounds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.UUID;

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

		ServerPlayNetworking.registerGlobalReceiver(MissileModePacket.ID, (server, player, handler, buf, responseSender) -> {
			server.execute(() -> {
				CommandLaunchUnitHandler.handleMissileChangeMode(player);
			});
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			ServerPlayerEntity player = handler.player;
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
		});
/*
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			server.getWorlds().forEach(world -> {

				MissilePersistentState persistentState = MissilePersistentState.getServerState(world.getPersistentStateManager());
                NbtCompound loadedStates = persistentState.getMissileStates();
				if (!loadedStates.isEmpty()) {
					for (String key : loadedStates.getKeys()) {
						NbtCompound missileData = loadedStates.getCompound(key);
						reloadMissileEntity(world, missileData);
						AdvancedMissile.LOGGER.info("Loaded missile!");
					}
				} else {
					AdvancedMissile.LOGGER.info("No saved data for missiles!");
				}

			});

		});




		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			server.getWorlds().forEach(world -> {

				NbtCompound missilesStates = new NbtCompound();

				world.getEntitiesByType(ModEntities.MISSILE, entity -> true).forEach(entity -> {
					if (entity != null) {

						AdvancedMissile.LOGGER.info("Saved a missile!");

						NbtCompound nbt = new NbtCompound();

						entity.writeNbt(nbt);

						missilesStates.put(String.valueOf(entity.getUuid()), nbt);
					}
				});

				//PersistentStateManager persistentStateManager = world.getPersistentStateManager();
				MissilePersistentState persistentState = MissilePersistentState.getServerState(world.getPersistentStateManager());
				persistentState.setMissileStates(missilesStates);

			});
		});

 */

		ModEntities.registerModEntities();

		ModItemGroups.registerItemGroups();
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();
		ModBlockEntities.registerBlockEntities();

		ModScreenHandlers.registerScreenHandlers();

		ModRecipes.registerRecipes();

		ModSounds.registerSounds();

	}

	private void reloadMissileEntity(ServerWorld world, NbtCompound nbt) {
		MissileEntity missileEntity = new MissileEntity(world, nbt);
        missileEntity.readNbt(nbt);
        world.spawnEntity(missileEntity);
    }
}