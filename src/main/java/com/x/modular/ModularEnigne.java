package com.x.modular;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.x.game.player.Player;
import com.x.injection.ApplicationContext;
import com.x.injection.ApplicationContext.Initializer;
import com.x.modular.code.GenMC;
import com.x.modular.code.GenML;
import com.x.modular.code.ModularAnalyzer;
import com.x.modular.code.ModularAnalyzer.ModularInfo;

import javassist.ClassPool;

/**
 * 
 * @author 
 * 
 */
public class ModularEnigne {
	
	private ModularEnigne() {
	}
	
	/**
	 * 依赖于ApplicationContext初始化
	 * @param assembleClazz
	 * @param executor
	 */
	public static void initialize(final Class<? extends Player> assembleClazz) {
	    ApplicationContext.registBean(new Initializer() {
            @Override
            public void initialize(List<Class<?>> clazzes) {
                ModularEnigne.initialize(assembleClazz, clazzes);
                ApplicationContext.registBean(ModuleLoader.class, GenML.generate(mccn, load));
            }
        });
	}
	
	/**
	 * 执行模块化 启动工作
	 * @param assembleClazz (moduleContainer)
	 * @param clazzes
	 */
	public static void initialize(Class<? extends Player> assembleClazz, List<Class<?>> clazzes) {
		try {
		    Map<Class<?>, Integer> indexes = new HashMap<Class<?>, Integer>();
            Set<Class<?>> bridgeClazzes = new HashSet<Class<?>>();
            
            List<ModularInfo> infos = ModularAnalyzer.analye(clazzes, indexes, bridgeClazzes);
            ClassPool pool = ClassPool.getDefault();
            
            mccn = GenMC.generate(pool, assembleClazz, infos);
            load = GenML.generate(pool, mccn, indexes);
            
            //add module classes for prototype dependence check
            infos.stream().filter(info->info.proxyClazz!=null).forEach(info->clazzes.add(info.proxyClazz));
        } catch (Throwable e) {
            throw new IllegalArgumentException("Modular enigne initialize: ", e);
        }
	}
	
	/**
	 * module container class name
	 */
	static String mccn;
	static Map<Class<?>, ModuleTypeLoader> load;
	
	static <T extends Module<?>> T loadModule(ModuleContainer container, Class<T> clazz) {
        return load.get(clazz).load(container);
    }
	
    public static ModuleTypeLoader fetchLoader(Class<?> clazz) {
        return load.get(clazz);
    }

    public static String fetchMCClassName() {
        return mccn;
    }
	
}
