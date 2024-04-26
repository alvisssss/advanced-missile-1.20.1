package net.alvisssss.advancedmissile.entity.client;

import net.alvisssss.advancedmissile.AdvancedMissile;
import net.alvisssss.advancedmissile.entity.custom.MissileEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.TridentEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;

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

    @Override
    public void render(MissileEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.scale(1f,1f,1f);
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    public MissileModel getModel() {
        return this.model;
    }
}
