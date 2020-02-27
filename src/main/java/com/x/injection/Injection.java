package com.x.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Injection {
    
    private static final Logger logger = LoggerFactory.getLogger(Injection.class);
    
    private static Map<Class<?>, Injector> injectors = new HashMap<>();
    
    public static List<Class<?>> injectedClasses() {
        return injectedClasses(null);
    }
    public static List<Class<?>> injectedClasses(Class<? extends Annotation> presentAnno) {
        return injectors.keySet().stream().filter(c->presentAnno==null||c.isAnnotationPresent(presentAnno)).collect(Collectors.toList());
    }
    
    public static Injector getInjector(Class<?> clazz) {
        return injectors.get(clazz);
    }
    
    public static Object inject(Object bean) {
        return inject(bean, Injector.build(bean.getClass()));
    }
    
    public static Object makeInstanceAndInject(Class<?> clazz) {
        return makeInstanceAndInject(clazz, Injector.build(clazz));
    }
    
    public static Object makeInstanceAndInject(Class<?> clazz, Injector injector) {
        try {
            injectors.put(clazz, injector);
            
        	Constructor<?> con = clazz.getDeclaredConstructor();
        	con.setAccessible(true);
        	Object bean = con.newInstance();
            inject(bean, injector);
            return bean;
        } catch (Throwable e) {
            throw new IllegalArgumentException(clazz.getName(), e);
        }
    }

    public static Object inject(Object bean, Injector injector) {
        injector.inject(bean);
        load(bean);
        return bean;
    }
    
    public static void load(Object bean) {
        if(bean instanceof Loadable) {
            long start = System.currentTimeMillis();
            ((Loadable) bean).load();
            long used = System.currentTimeMillis() - start;
            logger.info("Load completed {} used {}ms", bean.getClass().getName(), used);
        }
    }

}
