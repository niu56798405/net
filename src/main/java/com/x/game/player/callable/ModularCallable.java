package com.x.game.player.callable;

import java.util.HashMap;
import java.util.Map;

import com.x.game.player.Callable;
import com.x.game.player.ModularPlayer;
import com.x.modular.ModularEnigne;
import com.x.modular.Module;
import com.x.modular.ModuleTypeLoader;
import com.x.tools.Generic;

/**
 * 
 * @author 
 *
 * @param <T>
 * @param <V>
 */
public abstract class ModularCallable<T extends ModularPlayer, V extends Module<?>> implements Callable<T> {
	
	final static Map<Class<?>, ModuleTypeLoader> loaders = new HashMap<>();
	
	final static ModuleTypeLoader getLoader(Class<?> clazz) {
		ModuleTypeLoader loader = loaders.get(clazz);
		if(loader == null) {
			loader = ModularEnigne.fetchLoader(Generic.analyse(clazz, ModularCallable.class).getByGenericName("V"));
			loaders.put(clazz, loader);
		}
		return loader;
	}
    
    public final void call(T player) {
        call(player, getLoader(this.getClass()).<V>load(player));
    }
    
    protected abstract void call(T player, V module);

}
