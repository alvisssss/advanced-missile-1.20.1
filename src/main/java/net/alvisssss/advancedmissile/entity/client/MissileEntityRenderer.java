package net.alvisssss.advancedmissile.entity.client;

import net.alvisssss.advancedmissile.AdvancedMissile;
import net.alvisssss.advancedmissile.entity.custom.MissileEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class MissileEntityRenderer extends EntityRenderer<MissileEntity> {

    private final MissileModel model;
    private static final Identifier TEXTURE = new Identifier(AdvancedMissile.MOD_ID, "textures/entity/texture.png");
    public MissileEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.model = new MissileModel<>(ctx.getPart(ModModelLayers.MISSILE));
    }

    @Override
    public Identifier getTexture(MissileEntity entity) {
        return TEXTURE;
    }

    // Renders the entire missile model with settings such as brightness.
    @Override
    public void render(MissileEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        this.model.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityCutout(TEXTURE)), light, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }
}
