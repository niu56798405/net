package com.x.modular;

import com.x.injection.Combine;

@Combine
public interface ModuleLoader {
    
    /**
     * 从player中加载某个模块
     * @param player
     * @param clazz
     * @return
     */
    public <T extends Module<?>> T loadModule(ModuleContainer container, Class<T> clazz);
    
}
