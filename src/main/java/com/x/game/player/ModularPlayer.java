package com.x.game.player;

import com.x.action.ActionQueue;
import com.x.modular.ModularType;
import com.x.modular.ModuleContainer;

public abstract class ModularPlayer extends Player implements ModuleContainer {

    private ModularType dataType;
    
    public ModularPlayer(long playerId, ActionQueue queue) {
        super(playerId, queue);
    }
    
    public boolean load(ModularType type) {
        if(type == ModularType.TRANSIENT) dataType = ModularType.TRANSIENT;
        if(type == ModularType.RESIDENT && dataType != ModularType.TRANSIENT) dataType = ModularType.RESIDENT;
        return true;
    }
    
    public boolean unload(ModularType type) {
        if(type == ModularType.TRANSIENT) dataType = ModularType.RESIDENT;
        if(type == ModularType.RESIDENT) dataType = null;
        return true;
    }
    
    @Override
    public final boolean load() {
        return this.load(ModularType.RESIDENT);
    }
    
    @Override
    public final boolean idle(long activeTime) {
        if(isUnloadData(ModularType.TRANSIENT, activeTime)) {
            this.unload(ModularType.TRANSIENT);
        }
        if(isUnloadData(ModularType.RESIDENT, activeTime)) {
            this.unload(ModularType.RESIDENT);
            return true;
        }
        return false;
    }
    
    private boolean isUnloadData(ModularType dataType, long activeTime) {
        return this.unloadable(dataType) && (System.currentTimeMillis() - activeTime) > dataType.unloadIdleTime;
    }
    
    public boolean save() {
        return true;
    }
    
    public boolean isUnloaded(ModularType type) {
        if(type == ModularType.RESIDENT) return this.dataType == null;
        if(type == ModularType.TRANSIENT) return this.dataType == ModularType.RESIDENT;
        return false;
    }

}
