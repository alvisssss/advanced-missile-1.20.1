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
        //HandledScreens.register(ModScreenHandlers.TURRET_SETTING_SCREEN_HANDLER, TurretSettingScreen::new);

        HandledScreens.register(ModScreenHandlers.UPGRADING_SCREEN_HANDLER, UpgradingScreen::new);

        EntityRendererRegistry. register(ModEntities.MISSILE, MissileEntityRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.MISSILE, MissileModel::getTexturedModelData);
    }
}
