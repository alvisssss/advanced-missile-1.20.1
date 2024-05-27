package net.alvisssss.advancedmissile.mixin;

import net.alvisssss.advancedmissile.item.custom.CommandLaunchUnitItem;
import net.alvisssss.advancedmissile.util.IFovChangeMixin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class FovChangeMixin {

    @Inject(method = "getFovMultiplier", at = @At("RETURN"), cancellable = true)
    public void modifyFov(CallbackInfoReturnable<Float> cir) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

        if (player.getStackInHand(player.getActiveHand()).getItem() instanceof CommandLaunchUnitItem && player.isUsingItem()
        && MinecraftClient.getInstance().options.getPerspective().isFirstPerson()) {
            cir.setReturnValue(0.1f);
        }
    }
}
