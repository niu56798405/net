package com.x.modular;

public interface ModuleContainer {
    
    public boolean load(ModularType type);
    
    public boolean unload(ModularType type);
    
    public boolean save();
    
    public boolean isUnloaded(ModularType type);
    
    public boolean unloadable(ModularType type);

}
