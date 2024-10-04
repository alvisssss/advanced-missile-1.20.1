package net.alvisssss.advancedmissile.mixin;

import net.alvisssss.advancedmissile.item.custom.CommandLaunchUnitItem;
import net.alvisssss.advancedmissile.util.SightManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Mouse.class)
public abstract class MouseScrollMixin {

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void modifyZoom(long window, double horizontal, double vertical, CallbackInfo callbackInfo) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null
                && client.player.isUsingItem()
                && client.player.getStackInHand(client.player.getActiveHand()).getItem() instanceof CommandLaunchUnitItem
                && client.options.getPerspective().isFirstPerson()
        ) {
            SightManager.changeZoomLevel(vertical);
            callbackInfo.cancel(); // Need to smooth out the movement of zooming and camera.
        }
    }
}
