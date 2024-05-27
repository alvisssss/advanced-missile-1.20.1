package net.alvisssss.advancedmissile.sound;

import net.alvisssss.advancedmissile.AdvancedMissile;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {

    public static final SoundEvent MISSILE_RELOAD_FAIL = registerSoundEvent("missile_fire_fail");

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = new Identifier(AdvancedMissile.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }
    public static void registerSounds() {
        AdvancedMissile.LOGGER.info("Registering Sounds for " + AdvancedMissile.MOD_ID);
    }
}
