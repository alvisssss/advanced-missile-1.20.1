package net.alvisssss.advancedmissile;

import net.alvisssss.advancedmissile.entity.ModEntities;
import net.alvisssss.advancedmissile.entity.client.MissileEntityRenderer;
import net.alvisssss.advancedmissile.entity.client.MissileModel;
import net.alvisssss.advancedmissile.entity.client.ModModelLayers;
import net.alvisssss.advancedmissile.item.custom.CommandLaunchUnitItem;
import net.alvisssss.advancedmissile.keybind.Keybindings;
import net.alvisssss.advancedmissile.keybind.ReloadHandler;
import net.alvisssss.advancedmissile.network.MissileFirePacket;
import net.alvisssss.advancedmissile.network.MissileModePacket;
import net.alvisssss.advancedmissile.screen.ModScreenHandlers;
import net.alvisssss.advancedmissile.screen.UpgradingScreen;
import net.alvisssss.advancedmissile.util.TargetEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.util.Hand;

public class AdvancedMissileClient implements ClientModInitializer {


    @Override
    public void onInitializeClient() {

        Keybindings.registerKeybindings();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (Keybindings.fireKey.wasPressed()) {
                if (client.player != null && client.player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof CommandLaunchUnitItem) {
                    MissileFirePacket.send();
                }
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (Keybindings.modeKey.wasPressed()) {
                if (client.player != null && client.player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof CommandLaunchUnitItem) {
                    MissileModePacket.send();
                }
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> ReloadHandler.tick());

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null
                    && client.player.isUsingItem()
                    && client.options.getPerspective().isFirstPerson()
                    && client.player.getStackInHand(client.player.getActiveHand()).getItem() instanceof CommandLaunchUnitItem) {
                TargetEntityRenderer.resetUuidDoubleHashMap();
                TargetEntityRenderer.drawBoundingBoxesForAllEntities(client, drawContext, tickDelta);
            }
        });


        HandledScreens.register(ModScreenHandlers.UPGRADING_SCREEN_HANDLER, UpgradingScreen::new);
        EntityRendererRegistry. register(ModEntities.MISSILE, MissileEntityRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.MISSILE, MissileModel::getTexturedModelData);

    }
}
