package net.alvisssss.advancedmissile.item;

import net.alvisssss.advancedmissile.AdvancedMissile;
import net.alvisssss.advancedmissile.item.custom.*;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    // New items
    public static final Item LAUNCHER = registerItem("launcher", new LauncherItem(new FabricItemSettings().maxCount(1))); // Uses LauncherItem class, only 1 item per stack.
    public static final Item MISSILE = registerItem("missile", new MissileItem(new FabricItemSettings())); // Uses MissileItem class.
    public static final Item LOCATOR = registerItem("locator", new LocatorItem(new FabricItemSettings().maxCount(1))); // Uses LocatorItem class, only 1 item per stack.
    public static final Item CLU = registerItem("command_launch_unit", new CommandLaunchUnitItem(new FabricItemSettings().maxCount(1)));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(AdvancedMissile.MOD_ID, name), item);
    }
    public static void registerModItems() {
        AdvancedMissile.LOGGER.info("Registering Mod Items for " + AdvancedMissile.MOD_ID);
    }

}
