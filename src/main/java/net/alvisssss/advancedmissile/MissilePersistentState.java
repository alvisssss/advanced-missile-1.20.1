package net.alvisssss.advancedmissile;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

public class MissilePersistentState extends PersistentState {

    private static Type<MissilePersistentState> type = new Type<>(
             MissilePersistentState::new, // If there's no 'StateSaverAndLoader' yet create one
            MissilePersistentState::createFromNbt, // If there is a 'StateSaverAndLoader' NBT, parse it with 'createFromNbt'
            null // Supposed to be an 'DataFixTypes' enum, but we can just pass null
    );

    private final NbtCompound missileStates = new NbtCompound();

    public static MissilePersistentState createFromNbt(NbtCompound nbt) {
        MissilePersistentState state = new MissilePersistentState();
        state.missileStates.copyFrom(nbt);
        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) { // Called once per world
        nbt.put(AdvancedMissile.MOD_ID, this.missileStates);
        return nbt;
    }

    public void setMissileStates(NbtCompound states) { // Called once per world
        this.missileStates.copyFrom(states);
        markDirty();
    }
    public NbtCompound getMissileStates() {
        return this.missileStates;
    }
    public static MissilePersistentState getServerState(PersistentStateManager persistentStateManager) {
        MissilePersistentState state = persistentStateManager.getOrCreate(type, AdvancedMissile.MOD_ID);
        state.markDirty(); // Redundant?
        return state;
    }


}
