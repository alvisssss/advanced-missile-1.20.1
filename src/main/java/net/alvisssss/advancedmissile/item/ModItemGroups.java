package net.alvisssss.advancedmissile.item;

import net.alvisssss.advancedmissile.AdvancedMissile;
import net.alvisssss.advancedmissile.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {

    // Registers all items into a tab in the creative inventory, with the missile texture as the logo.
    public static final ItemGroup MISSILE_GROUP = Registry.register(Registries.ITEM_GROUP,
            new Identifier(AdvancedMissile.MOD_ID, "launcher"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.launcher"))
                    .icon(() -> new ItemStack(ModItems.TOMAHAWK_MISSILE)).entries((displayContext, entries) -> {

                        entries.add(ModItems.TOMAHAWK_MISSILE);
                        entries.add(ModItems.JAVELIN_MISSILE);
                        entries.add(ModItems.LAUNCHER);
                        entries.add(ModItems.LOCATOR);

                        entries.add(ModItems.CLU);

                        entries.add(ModBlocks.TURRET);
                        entries.add(ModBlocks.UPGRADING_FACTORY);

                        entries.add(ModBlocks.REINFORCED_OBSIDIAN_LEVEL_1);
                        entries.add(ModBlocks.REINFORCED_OBSIDIAN_LEVEL_2);

                    }).build());

    public static void registerItemGroups() {
        AdvancedMissile.LOGGER.info("Registering Item Groups for " + AdvancedMissile.MOD_ID);
    }
}
