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
    // Registers Turret Station (Work in Progress) which uses the class TurretBlock.
    public static final Block TURRET_STATION = registerBlock("turret_station",
            new TurretBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK))); // Settings copied from Iron Block.

    // Registers Upgrading Factory Block which uses the class UpgradingFactoryBlock.
    public static final Block UPGRADING_FACTORY = registerBlock("upgrading_factory",
            new UpgradingFactoryBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK))); // Settings copied from Iron Block.


    // Registers the blocks added.
    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(AdvancedMissile.MOD_ID, name), block);
    }

    // Registers the items for the blocks. (With 3d view).
    // Items and Blocks are separate: Items are what the player can have in their inventory.
    // Blocks are the voxels in the world.
    // A block doesn't necessarily have to have its item form, and vice versa.
    private static Item registerBlockItem(String name, Block block) {
        return Registry.register(Registries.ITEM, new Identifier(AdvancedMissile.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings()));
    }

    public static void registerModBlocks() {
        AdvancedMissile.LOGGER.info("Registering ModBlocks for " + AdvancedMissile.MOD_ID);
    }
}
