package net.alvisssss.advancedmissile.keybind;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class Keybindings {
    public static KeyBinding reloadKey;
    public static KeyBinding fireKey;
    public static void registerKeybindings() {
        fireKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.advancedmissile.fire_missile",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_ALT,
                "category.advancedmissile.controls"
        ));

        reloadKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.advancedmissile.reload_missile",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.advancedmissile.controls"
        ));
    }
}
