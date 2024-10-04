package net.alvisssss.advancedmissile.mixin;

import net.alvisssss.advancedmissile.item.custom.CommandLaunchUnitItem;
import net.alvisssss.advancedmissile.util.SightManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"), cancellable = true)
    public <E extends Entity> void customRender(E entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (entity instanceof LivingEntity
                && MinecraftClient.getInstance().player != null
                && MinecraftClient.getInstance().player.canSee(entity)
                && MinecraftClient.getInstance().player.isUsingItem()
                && MinecraftClient.getInstance().options.getPerspective().isFirstPerson()
                && MinecraftClient.getInstance().player.getStackInHand(MinecraftClient.getInstance().player.getActiveHand()).getItem() instanceof CommandLaunchUnitItem) {
            Box box = entity.getBoundingBox().offset(-entity.getX(), -entity.getY(), -entity.getZ());
            if (entity.getUuid().equals(SightManager.getTargetUuid())) {
                WorldRenderer.drawBox(matrices, vertexConsumers.getBuffer(RenderLayer.getLines()), box, 1.0f, 0.0f, 0.0f, 1.0f);
            } else {
                WorldRenderer.drawBox(matrices, vertexConsumers.getBuffer(RenderLayer.getLines()), box, 0.957f, 0.643f, 0.298f, 1.0f);
            }
        }
    }
}
