package net.alvisssss.advancedmissile;

import net.alvisssss.advancedmissile.entity.ModEntities;
import net.alvisssss.advancedmissile.entity.client.MissileEntityRenderer;
import net.alvisssss.advancedmissile.entity.client.MissileModel;
import net.alvisssss.advancedmissile.entity.client.ModModelLayers;
import net.alvisssss.advancedmissile.screen.ModScreenHandlers;
import net.alvisssss.advancedmissile.screen.UpgradingScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class AdvancedMissileClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        HandledScreens.register(ModScreenHandlers.UPGRADING_SCREEN_HANDLER, UpgradingScreen::new); // Registers GUI

        EntityRendererRegistry. register(ModEntities.MISSILE, MissileEntityRenderer::new); // Registers Missile Entity to use MissileEntityRenderer.
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.MISSILE, MissileModel::getTexturedModelData); // Registers Missile Entity to have a MissileModel.
    }
}
