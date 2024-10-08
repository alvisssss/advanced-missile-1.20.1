package net.alvisssss.advancedmissile.block.entity;

import net.alvisssss.advancedmissile.AdvancedMissile;
import net.alvisssss.advancedmissile.block.ModBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {

    public static final BlockEntityType<TurretBlockEntity> TURRET_BLOCK_ENTITY =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(AdvancedMissile.MOD_ID, "turret_be"),
                    FabricBlockEntityTypeBuilder.create(TurretBlockEntity::new,
                            ModBlocks.TURRET).build());

    public static void registerBlockEntities() {
        AdvancedMissile.LOGGER.info("Registering Block Entities for " + AdvancedMissile.MOD_ID);
    }
}
