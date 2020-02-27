package com.x.injection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.x.injection.Injector.InjectField;
import com.x.injection.code.Codes;
import com.x.tools.Bubble;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.NewExpr;

public class Dependences {
    
    private Map<Class<?>, Injector> injectors;
    
    private int index;

    private List<Class<?>> orderedClasses;
    
    private Map<Class<?>, List<Class<?>>> refPrototypes;
    
    public Dependences() {
        this.index = 0;
        this.orderedClasses = new ArrayList<>();
        this.injectors = new HashMap<>();
        this.refPrototypes = new HashMap<>();
    }
    
    private boolean isBackup(Class<?> clazz) {
        Bean anno = clazz.getAnnotation(Bean.class);
        return anno != null && anno.backup();
    }
    
    private boolean exists(Class<?> clazz) {
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> interfaze : interfaces) {
            if(!injectors.containsKey(interfaze) || !exists(interfaze)) return false;
        }
        
        Class<?> superClazz = clazz.getSuperclass();
        if(superClazz != null && superClazz != Object.class) {
            if(!injectors.containsKey(superClazz) || !exists(superClazz)) return false;
        }
        
        return true;
    }
    
    public Dependences analyse(List<Class<?>> orignalClasses, Predicate predicate) {
        List<Class<?>> backups = new ArrayList<>();
        for (Class<?> clazz : orignalClasses) {
            if(predicate.test(clazz)) {
                if(isBackup(clazz)) {
                    backups.add(clazz);
                } else {
                    orderedClasses.add(clazz);
                    putUpwardIfIsBean(injectors, clazz, Injector.build(clazz));
                }
            }
        }
        
        for (Class<?> backup : backups) {
            if(!exists(backup)) {
                orderedClasses.add(backup);
                putUpwardIfIsBean(injectors, backup, Injector.build(backup));
            }
        }
        
        Bubble.sort(orderedClasses, new Comparator<Class<?>>() {//首先按字母排序,保证每次加载顺序一样
            @Override
            public int compare(Class<?> o1, Class<?> o2) {
                return o1.getSimpleName().compareTo(o2.getSimpleName());
            }
        });
        Bubble.sort(orderedClasses, new Comparator<Class<?>>() {//Prototype优先加载
            @Override
            public int compare(Class<?> o1, Class<?> o2) {
                return o1.getAnnotation(Prototype.class) == null && o2.getAnnotation(Prototype.class) != null ? 1 : 0;//o2为Prototype o1不为Prototype, 换位置(o1 > o2)
            }
        });
        Bubble.sort(orderedClasses, new Comparator<Class<?>>() {//按@Bean.prior排序,把@Bean.prior放至最前
            @Override
            public int compare(Class<?> o1, Class<?> o2) {
                //o2为prior bean且o1不为prior bean
                return (o2.isAnnotationPresent(Bean.class) && o2.getAnnotation(Bean.class).prior()) && (!o1.isAnnotationPresent(Bean.class) || !o1.getAnnotation(Bean.class).prior()) ? 1 : 0;
            }
        });
        
        for (Class<?> key : orderedClasses) {
            analyse0(key, new DependeceLink(null, null));
        }
        
        Bubble.sort(orderedClasses, new Comparator<Class<?>>() {//把@Bean.prior放置Prototype后
            @Override
            public int compare(Class<?> o1, Class<?> o2) {
                return o1.getAnnotation(Prototype.class) == null && o2.getAnnotation(Prototype.class) != null ? 1 : 0;//o2为Prototype o1不为Prototype, 换位置(o1 > o2)
            }
        });
        Bubble.sort(orderedClasses, new Comparator<Class<?>>() {//按加载顺序加载
            @Override
            public int compare(Class<?> o1, Class<?> o2) {
                return (o1.isAnnotationPresent(Prototype.class) ^ o2.isAnnotationPresent(Prototype.class)) ? 0 : injectors.get(o1).index - injectors.get(o2).index;
            }
        });
        return this;
    }

    public void forEach(Consumer consumer) {
        for (Class<?> clazz : orderedClasses) {
            consumer.accept(this, clazz);
        }
    }
    
    public Injector getInjector(Class<?> clazz) {
        return injectors.get(clazz);
    }
    
    private void analyse0(Class<?> key, DependeceLink parent) {
        parent.checkCircularDependence();
        Injector injector = injectors.get(key);
        if(injector == null || injector.index > 0) return;//已经确定顺序或者不是bean(其他)
        InjectField[] fields = injector.fields;
        Arrays.stream(fields).filter(f->!f.isLazy).forEach(f->analyse0(f.type(), new DependeceLink(parent, f.type())));
        InjectDependence anno = injector.master.getAnnotation(InjectDependence.class);
        if(anno != null) Arrays.stream(anno.value()).forEach(c->analyse0(c, new DependeceLink(parent, c)));
        getRefPrototypes(injector.master).forEach(c->analyse0(c, new DependeceLink(parent, c)));;
        injector.index = ++index;
    }
    
    public <T> void putUpwardIfIsBean(Map<Class<?>, T> map, Class<?> key, T val) {
        putUpward(map, key, val, key.isAnnotationPresent(Bean.class));
    }
    
    public <T> void putUpward(Map<Class<?>, T> map, Class<?> key, T val, boolean clazzUpWard) {
        map.put(key, val);
        
        for (Class<?> interfaze : key.getInterfaces())
            putUpward(map, interfaze, val, clazzUpWard);
        
        if(clazzUpWard) {
            Class<?> superClazz = key.getSuperclass();
            if(superClazz != null && superClazz != Object.class)
                putUpward(map, superClazz, val, clazzUpWard);
        }
    }
    
    private List<Class<?>> getRefPrototypes(Class<?> clazz) {
        if(!refPrototypes.containsKey(clazz)) {
            try {
                ClassPool pool = ClassPool.getDefault();
                CtClass ct = pool.get(clazz.getName());
                
                List<String> refs = new ArrayList<>();
                List<CtBehavior> behaviors = getRefMethods(clazz, ct);
                
                for (CtBehavior behavior : behaviors) {
                    behavior.getDeclaringClass().defrost();
                    behavior.instrument(new ExprEditor(){
                        public void edit(NewExpr e) throws CannotCompileException {
                            refs.add(e.getClassName());
                        }
                    });
                }
                refPrototypes.put(clazz, Codes.loadClasses(refs).stream().filter(c->c.isAnnotationPresent(Prototype.class)).collect(Collectors.toList()));
            } catch (NotFoundException | CannotCompileException e) {
                //ignore
            }
        }
        return refPrototypes.get(clazz);
    }
    private List<CtBehavior> getRefMethods(Class<?> clazz, CtClass ct) throws NotFoundException {
        List<CtBehavior> behaviors = new ArrayList<>(Arrays.asList(ct.getConstructors()));
        if(Loadable.class.isAssignableFrom(clazz)) {
            behaviors.add(ct.getMethod("load", "()V"));
        }
        return behaviors;
    }

    @FunctionalInterface
    public static interface Predicate {
        public boolean test(Class<?> c);
    }
    @FunctionalInterface
    public static interface Consumer {
        public void accept(Dependences d, Class<?> c);
    }
    
    static class DependeceLink {
        public final DependeceLink parent;
        public final Class<?> self;
        public DependeceLink(DependeceLink parent, Class<?> self) {
            this.parent = parent;
            this.self = self;
        }
        public void checkCircularDependence() {
            if(this.self != null) {
                DependeceLink link = this;
                StringBuilder linkStr = new StringBuilder();
                while(link.parent != null && link.parent.self != null) {
                    linkStr.append(link.self.getName()).append(" ");
                    if(this.self.equals(link.parent.self)) {
                        throw new IllegalArgumentException(String.format("Circular dependence: %s", linkStr.append(this.self.getName())));
                    }
                    link = link.parent;
                }
            }
        }
    }
    
}
