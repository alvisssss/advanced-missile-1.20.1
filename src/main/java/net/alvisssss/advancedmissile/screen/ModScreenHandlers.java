package net.alvisssss.advancedmissile.screen;


import net.alvisssss.advancedmissile.AdvancedMissile;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreenHandlers {/*
    public static final ScreenHandlerType<TurretSettingScreenHandler> TURRET_SETTING_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, new Identifier(AdvancedMissile.MOD_ID, "turret_setting"),
                    new ExtendedScreenHandlerType<>(TurretSettingScreenHandler::new));
                    */

    public static final ScreenHandlerType<UpgradingScreenHandler> UPGRADING_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, new Identifier(AdvancedMissile.MOD_ID, "upgrading"),
                    new ExtendedScreenHandlerType<>(UpgradingScreenHandler::new));


    public static void registerScreenHandlers() {
        AdvancedMissile.LOGGER.info("Registering Screen Handlers for " + AdvancedMissile.MOD_ID);
    }
}
