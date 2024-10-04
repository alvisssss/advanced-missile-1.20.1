package net.alvisssss.advancedmissile.keybind;

import net.alvisssss.advancedmissile.item.custom.CommandLaunchUnitItem;
import net.alvisssss.advancedmissile.sound.ModSounds;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.world.World;


public class CommandLaunchUnitHandler {
    public static void handleMissileFire(ServerPlayerEntity player) {
        World world = player.getWorld();
        if (player.getMainHandStack().getItem() instanceof CommandLaunchUnitItem) {
            ItemStack itemStack = player.getMainHandStack();
            if (CommandLaunchUnitItem.isLoaded(itemStack)) {
                CommandLaunchUnitItem.shootAll(world, player, player.getActiveHand(), itemStack, CommandLaunchUnitItem.getMissileSpeed());
                CommandLaunchUnitItem.setLoaded(itemStack, false);
            } else {
                world.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.MISSILE_RELOAD_FAIL, SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
        }

    }
    public static void handleMissileReload(ServerPlayerEntity player) {
        ItemStack itemStack = player.getMainHandStack();
        if (itemStack.getItem() instanceof CommandLaunchUnitItem && !CommandLaunchUnitItem.isLoaded(itemStack) && CommandLaunchUnitItem.loadMissiles(player, itemStack)) {
            CommandLaunchUnitItem.setLoaded(itemStack, true);
                CommandLaunchUnitItem.setPreciseMode(itemStack,
                        itemStack.getNbt() != null && itemStack.getNbt().contains(CommandLaunchUnitItem.CHANGE_MODE_KEY) && CommandLaunchUnitItem.getPreciseMode(itemStack));
        }
    }

    public static void handleMissileChangeMode(ServerPlayerEntity player) {
        ItemStack itemStack = player.getMainHandStack();
        CommandLaunchUnitItem.setPreciseMode(itemStack, !CommandLaunchUnitItem.getPreciseMode(itemStack));
        player.sendMessage(Text.literal("Missile Mode: " + (CommandLaunchUnitItem.getPreciseMode(itemStack) ? "Precise" : "Normal")));
    }
}
