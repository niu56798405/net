package com.x.modular.code;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.x.modular.Modular;
import com.x.modular.ModularBridge;
import com.x.modular.ModularDependence;
import com.x.modular.ModularIgnore;
import com.x.tools.Bubble;

/**
 * 分析各种annotation
 * 加载关系等
 * @author 
 */
public class ModularAnalyzer {

    public static class ModularInfo {
        public final Class<?> clazz;
        public final int index;
        public Class<?> proxyClazz;
        public ModularInfo(Class<?> clazz, int index) {
            this.clazz = clazz;
            this.index = index;
        }
    }
    
    public static List<ModularInfo> analye(List<Class<?>> clazzes, Map<Class<?>, Integer> indexes, Set<Class<?>> bridgeClazzes) {
        Set<Class<?>> modularClazzes = new HashSet<Class<?>>();
        outter:
        for (Class<?> clazz : clazzes) {
            if(Modifier.isAbstract(clazz.getModifiers()) || clazz.isInterface()) {
                continue;
            }
            
            //modules
            if(isNecessaryModularClass(clazz)) {
                modularClazzes.add(clazz);
            }
            
            //executables
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if(method.getAnnotation(ModularBridge.class) != null) {
                    bridgeClazzes.add(clazz);
                    continue outter;
                }
            }
        }
        
        return analyeDependence(modularClazzes, indexes);
    }

	private static boolean isNecessaryModularClass(Class<?> clazz) {
		return clazz.getAnnotation(Modular.class) != null && clazz.getAnnotation(ModularIgnore.class) == null;
	}
    
    private static int index = 0;
    private static List<ModularInfo> analyeDependence(Set<Class<?>> modularClazzes, Map<Class<?>, Integer> indexes) {
        List<ModularInfo> modularInfos = new ArrayList<ModularInfo>();
        Bubble.sort(modularInfos, new Comparator<ModularInfo>() {
            @Override
            public int compare(ModularInfo o1, ModularInfo o2) {
                return o1.clazz.getSimpleName().compareTo(o2.clazz.getSimpleName());
            }
        });
        
        for (Class<?> clazz : modularClazzes) {
            TreeNode root = new TreeNode(clazz);
            analyeDependence(root, clazz, indexes, modularInfos, modularClazzes);
        }
        
        Bubble.sort(modularInfos, new Comparator<ModularInfo>() {
            @Override
            public int compare(ModularInfo o1, ModularInfo o2) {
                return o1.index - o2.index;
            }
        });
        return modularInfos;
    }
    
    private static void analyeDependence(TreeNode node, Class<?> clazz, Map<Class<?>, Integer> indexes, List<ModularInfo> modularInfos, Set<Class<?>> modularClazzes) {
        if(indexes.get(clazz) != null) { //已经加载完成
            return;
        }
        
        ModularDependence anno = clazz.getAnnotation(ModularDependence.class);
        if(anno != null) {//有依赖 优先加载依赖
            Class<?>[] dependences = anno.value();
            for (Class<?> dependence : dependences) {
                dependence = analyeAssignedDependence(clazz, dependence, modularClazzes);
                analyeCircularDependence(node, dependence);
                TreeNode self = new TreeNode(node, dependence);
                node.children.add(self);
                analyeDependence(self, dependence, indexes, modularInfos, modularClazzes);
            }
        }
        
        //依赖都已经加载完成
        indexes.put(clazz, index);
        modularInfos.add(new ModularInfo(clazz, index));
        ++index;
    }
    
    private static Class<?> analyeAssignedDependence(Class<?> clazz, Class<?> dependence, Set<Class<?>> modularClazzes) {
    	for (Class<?> modularClazz : modularClazzes) {
    		if(dependence.isAssignableFrom(modularClazz)) {
    			return modularClazz;
    		}
    	}
    	//can`t be here
		throw new IllegalArgumentException(
				"None modular clazz assigned to [" + dependence.getName() + "] (" + clazz.getName() + ")");
    }

    private static void analyeCircularDependence(TreeNode node, Class<?> clazz) {
        TreeNode tmp = node;
        while(tmp.parent != null) {
            if(tmp.parent.clazz == clazz) {
                throw new IllegalArgumentException("circular dependency by [" + tmp.parent.clazz.getName() + "] and [" + clazz.getName() + "]");
            }
            tmp = tmp.parent;
        }
    }

    static class TreeNode {
        final TreeNode parent;
        final Class<?> clazz;
        final List<TreeNode> children;
        public TreeNode(Class<?> clazz) {
            this(null, clazz);
        }
        public TreeNode(TreeNode parent, Class<?> clazz) {
            this.parent = parent;
            this.clazz = clazz;
            this.children = new ArrayList<TreeNode>();
        }
    }
    
}
