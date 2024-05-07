package net.alvisssss.advancedmissile.entity;

import net.alvisssss.advancedmissile.AdvancedMissile;
import net.alvisssss.advancedmissile.entity.custom.MissileEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {

    // Registers the missile using MissileEntity class.
    public static final EntityType<MissileEntity> MISSILE = Registry.register(Registries.ENTITY_TYPE,
            new Identifier(AdvancedMissile.MOD_ID, "missile"),
            FabricEntityTypeBuilder.<MissileEntity>create(SpawnGroup.MISC, MissileEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25f,0.25f)).build());
    public static void registerModEntities() {
        AdvancedMissile.LOGGER.info("Registering Mod Items for " + AdvancedMissile.MOD_ID);
    }
}
