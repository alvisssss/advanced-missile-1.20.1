package net.alvisssss.advancedmissile.keybind;

import net.alvisssss.advancedmissile.item.custom.CommandLaunchUnitItem;
import net.alvisssss.advancedmissile.network.MissileFirePacket;
import net.alvisssss.advancedmissile.network.MissileReloadPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class ReloadHandler {
    private static final int RELOAD_DURATION = 40;
    private static int reloadTickCounter = 0;
    private static boolean isReloading = false;

    public static void tick() {

        if (!Keybindings.reloadKey.isPressed()) {
            if (isReloading) {
                resetReload();
            }
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (player == null) {
            return;
        }

        ItemStack mainHandStack = player.getMainHandStack();

        if (!(mainHandStack.getItem() instanceof CommandLaunchUnitItem) || CommandLaunchUnitItem.isLoaded(mainHandStack)) {
            return;
        }

        if (Keybindings.reloadKey.isPressed()) {
            startReload();
            if (reloadTickCounter < RELOAD_DURATION) {
                reloadTickCounter++;
            }
            if (reloadTickCounter >= RELOAD_DURATION) {
                MissileReloadPacket.send();
                resetReload();
            }
        } else {
            resetReload();
        }
    }

    private static void startReload() {
        if (!isReloading) {
            isReloading = true;
            reloadTickCounter = 0;
        }
    }
    private static void resetReload() {
        isReloading = false;
        reloadTickCounter = 0;
    }
}
