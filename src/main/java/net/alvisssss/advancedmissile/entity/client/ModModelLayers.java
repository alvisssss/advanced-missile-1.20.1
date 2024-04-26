package net.alvisssss.advancedmissile.entity.client;

import net.alvisssss.advancedmissile.AdvancedMissile;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class ModModelLayers {
    public static final EntityModelLayer MISSILE =
            new EntityModelLayer(new Identifier(AdvancedMissile.MOD_ID, "missile"), "main");
}
