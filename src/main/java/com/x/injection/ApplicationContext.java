package com.x.injection;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.x.injection.code.Codes;
import com.x.injection.code.CombineBuilder;
import com.x.injection.code.ProxyBuilder;
import com.x.injection.factory.Factory;
import com.x.injection.factory.FactoryGenerator;
import com.x.net.cmd.CommandContext;

public class ApplicationContext {
    
    private static Map<Class<?>, Object> beanClassMap = new HashMap<Class<?>, Object>();
    private static Map<String, Object> beanNameMap = new HashMap<String, Object>();
    private static boolean isInitializing = false;
    
    public static void registBean(Object bean) {//通过类型区别
        Class<?> clazz = bean.getClass();
        registBean(clazz, bean);
    }

    public static void registBean(Class<?> clazz, Object bean) {//只注册一个接口
        beanClassMap.put(clazz, combineBean(clazz, bean));
        
        Class<?> superclazz = clazz.getSuperclass();
        if(superclazz != null && superclazz != Object.class)
            registBean(superclazz, bean);
        
        for(Class<?> interfaze : clazz.getInterfaces())
            registBean(interfaze, bean);
    }

    protected static Object combineBean(Class<?> clazz, Object bean) {
        if(clazz.isAnnotationPresent(Combine.class)) {
            Object exists = beanClassMap.get(clazz);
            if(exists == null) {
                exists = CombineBuilder.buildBean(clazz);
            }
            if(bean != null) {
                CombineBuilder.combine(exists, bean);
            }
            return exists;
        }
        return bean;
    }

    public static void registBean(String name, Object bean) {//通过名字区别
        beanNameMap.put(name, bean);
    }
    
    public static void initialize(String includes, String excludes) {
    	isInitializing = true;
        //empty Combine bean
        registBean(Initializer.class, null);
        registBean(Finalizer.class, null);
        
        initialize(Codes.getClasses(includes, excludes));
        isInitializing = false;
    }

    private static void initialize(List<Class<?>> classes) {
        fetchBean(Initializer.class).initialize(classes);
        
        loadFactories(classes);
        loadConfigurators(classes);
        loadRepositories(classes);//singleton
        loadTemplates(classes);//singleton
        
        loadBeans(classes);//singleton beans And Prototypes(prototype set injector)
        
        fetchBean(Finalizer.class).finalize(classes);
        loadEventuales(classes);
    }
    
    private static void loadFactories(List<Class<?>> classes) {
        classes.stream().filter(clazz -> clazz.isInterface() && clazz.isAnnotationPresent(Factory.class)).forEach(clazz -> registBean(clazz, FactoryGenerator.generate(clazz, classes)));
    }
    
    private static void loadConfigurators(List<Class<?>> classes) {
        new Dependences()
            .analyse(classes, (Class<?> c) -> !Modifier.isAbstract(c.getModifiers()) && !Modifier.isInterface(c.getModifiers()) && c.isAnnotationPresent(Configurator.class))
            .forEach((Dependences d, Class<?> c) -> registBean(Injection.makeInstanceAndInject(c, d.getInjector(c))));
    }
    
    private static void loadRepositories(List<Class<?>> classes) {
        new Dependences()
            .analyse(classes, (Class<?> c) -> !Modifier.isAbstract(c.getModifiers()) && !Modifier.isInterface(c.getModifiers()) && c.isAnnotationPresent(Repository.class))
            .forEach((Dependences d, Class<?> c) -> registBean(Injection.makeInstanceAndInject(c, d.getInjector(c))));
    }

    private static void loadTemplates(List<Class<?>> classes) {
        new Dependences()
            .analyse(classes, (Class<?> c) -> !Modifier.isAbstract(c.getModifiers()) && !Modifier.isInterface(c.getModifiers()) && c.isAnnotationPresent(Templates.class))
            .forEach((Dependences d, Class<?> c) -> registBean(c, ProxyBuilder.build(Injection.makeInstanceAndInject(c, d.getInjector(c)))));
    }
    
    private static void loadBeans(List<Class<?>> classes) {
        new Dependences()
            .analyse(classes, (Class<?> c) -> !Modifier.isInterface(c.getModifiers()) && !Modifier.isAbstract(c.getModifiers()) && (c.isAnnotationPresent(Bean.class) || c.isAnnotationPresent(Prototype.class)))
            .forEach((Dependences d, Class<?> c) -> {
                if(!c.isAnnotationPresent(Prototype.class)) //Prototype code patch时已经处理过了
                    registBean(Injection.makeInstanceAndInject(c, d.getInjector(c)));
            });
    }
    
    private static void loadEventuales(List<Class<?>> classes) {
        new Dependences()
            .analyse(classes, (Class<?> c) -> !Modifier.isInterface(c.getModifiers()) && !Modifier.isAbstract(c.getModifiers()) && c.isAnnotationPresent(Eventual.class))
            .forEach((Dependences d, Class<?> c) -> registBean(Injection.makeInstanceAndInject(c, d.getInjector(c))));
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T fetchBean(String name) {
        return (T) beanNameMap.get(name);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T fetchBean(Class<T> clazz) {
        return (T) beanClassMap.get(clazz);
    }
    
    public static void reloadTemplates() {
        Injection.injectedClasses(Templates.class).forEach(clazz->reloadTemplate(clazz));
    }

    public static void reloadTemplate(Class<?> clazz) {
        if(clazz.isAnnotationPresent(Templates.class)) {
            ProxyBuilder.setDelegate(fetchBean(clazz), Injection.makeInstanceAndInject(clazz, Injection.getInjector(clazz)));
        }
    }
    
    public static boolean isInitializing() {
    	return isInitializing;
    }
    
    /**
     * 在ApplicationContext.initialize之前执行
     * @author 
     */
    @Combine
    public static interface Initializer {
        public void initialize(List<Class<?>> clazzes);
    }
    
    @Combine
    public static interface Finalizer {
        public void finalize(List<Class<?>> clazzes);
    }
    
}