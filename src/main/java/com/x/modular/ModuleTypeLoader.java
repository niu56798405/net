package com.x.modular;

/**
 * 
 * 每一个Module Type对应一个Loader
 * @author 
 *
 */
public interface ModuleTypeLoader {
    
    public < T extends Module<?>> T load(ModuleContainer container);

}
