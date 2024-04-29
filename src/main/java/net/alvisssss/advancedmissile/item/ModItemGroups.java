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
    public static final ItemGroup MISSILE_GROUP = Registry.register(Registries.ITEM_GROUP,
            new Identifier(AdvancedMissile.MOD_ID, "launcher"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.launcher"))
                    .icon(() -> new ItemStack(ModItems.MISSILE)).entries((displayContext, entries) -> {

                        entries.add(ModItems.MISSILE);
                        entries.add(ModItems.LAUNCHER);
                        entries.add(ModItems.LOCATOR);

                        entries.add(ModBlocks.TURRET_STATION);
                        entries.add(ModBlocks.UPGRADING_FACTORY);
                    }).build());

    public static void registerItemGroups() {
        AdvancedMissile.LOGGER.info("Registering Item Groups for " + AdvancedMissile.MOD_ID);
    }
}
