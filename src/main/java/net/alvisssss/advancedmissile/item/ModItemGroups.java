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
                    .icon(() -> new ItemStack(ModItems.LAUNCHER)).entries((displayContext, entries) -> {
                        entries.add(ModItems.TURRET);
                        entries.add(ModItems.LAUNCHER);

                        entries.add(ModBlocks.TURRET_STATION);
                    }).build());

    private static void registerItemGroups() {
        AdvancedMissile.LOGGER.info("Registering Item Groups for " + AdvancedMissile.MOD_ID);
    }
}