package net.alvisssss.advancedmissile.block;

import net.alvisssss.advancedmissile.AdvancedMissile;
import net.alvisssss.advancedmissile.block.custom.TurretBlock;
import net.alvisssss.advancedmissile.block.custom.UpgradingFactoryBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {

    public static final Block TURRET = registerBlock("turret_station",
            new TurretBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)));

    public static final Block REINFORCED_OBSIDIAN_LEVEL_1 = registerBlock("reinforced_obsidian_level_1",
            new Block(FabricBlockSettings.copyOf(Blocks.CRYING_OBSIDIAN).strength(50.0f, 4800.0f)));

    public static final Block REINFORCED_OBSIDIAN_LEVEL_2 = registerBlock("reinforced_obsidian_level_2",
            new Block(FabricBlockSettings.copyOf(Blocks.CRYING_OBSIDIAN).strength(50.0f, 19200.0f)));

    public static final Block UPGRADING_FACTORY = registerBlock("upgrading_factory",
            new UpgradingFactoryBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)));

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(AdvancedMissile.MOD_ID, name), block);
    }

    private static Item registerBlockItem(String name, Block block) {
        return Registry.register(Registries.ITEM, new Identifier(AdvancedMissile.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings()));
    }

    public static void registerModBlocks() {
        AdvancedMissile.LOGGER.info("Registering ModBlocks for " + AdvancedMissile.MOD_ID);
    }
}
