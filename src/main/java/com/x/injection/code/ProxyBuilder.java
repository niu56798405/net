package com.x.injection.code;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMember;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.NotFoundException;

public class ProxyBuilder {
    
    public static interface Supplier {
        public Object get();
    }
    
    /**
     * try build
     * @param clazz
     * @return
     */
    public static boolean canProxy(Class<?> clazz) {
        if(clazz.isInterface()) return true;
        Constructor<?>[] constructors = clazz.getConstructors();
        for (Constructor<?> constructor : constructors) {
            if(constructor.getParameterTypes().length == 0) {
                return true;
            }
        }
        return false;
    }
    
    @SuppressWarnings("unchecked")
    public synchronized static <T> T build(T obj) {
        try {
            Class<?> type = obj.getClass();
            ClassPool pool = ClassPool.getDefault();
            CtClass ctParent = pool.get(type.getName());
            String lazyProxyName = type.getName() + "$$Proxy";
            
            Class<?> proxyClass = null;
            try {
                proxyClass = Class.forName(lazyProxyName);
            } catch(ClassNotFoundException e) {}
            
            if(proxyClass == null) {
                CtClass ctClass = pool.makeClass(lazyProxyName);
                if(Modifier.isInterface(type.getModifiers())) {
                    ctClass.addInterface(ctParent);
                } else {
                    ctClass.setSuperclass(ctParent);
                }
                CtField ctField0 = CtField.make("private " + type.getName() + " delegate;", ctClass);
                ctClass.addField(ctField0);
                
                CtConstructor ctConstructor = CtNewConstructor.make(new CtClass[]{pool.get(type.getName())}, new CtClass[0], ctClass);
                ctConstructor.setBody("{this.delegate=$1;}");
                ctClass.addConstructor(ctConstructor);
                
                List<CtMethod> ctMethods = getProxyMethods(ctParent);
                for (CtMethod ctMethod : ctMethods) {
                    StringBuilder body = new StringBuilder()
                        .append("{")
                        .append(ctMethod.getReturnType() == CtClass.voidType ? "" : "return ")
                        .append("this.delegate.").append(ctMethod.getName()).append("(");
                    for (int i = 0; i < ctMethod.getParameterTypes().length; i++) body.append((i==0?"":",")).append("$").append(i+1);
                    body.append(");}");
                    CtMethod ctnMethod = CtNewMethod.copy(ctMethod, ctClass, null);
                    ctnMethod.setBody(body.toString());
                    ctClass.addMethod(ctnMethod);
                }
                proxyClass = ctClass.toClass();
            }
            return (T) proxyClass.getConstructor(type).newInstance(obj);
        } catch (NotFoundException | RuntimeException | CannotCompileException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    @SuppressWarnings("unchecked")
    public synchronized static <T> T build(Class<T> type, Supplier supplier) {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctParent = pool.get(type.getName());
            String lazyProxyName = type.getName() + "$$SupplierProxy";
            
            Class<?> proxyClass = null;
            try {
                proxyClass = Class.forName(lazyProxyName);
            } catch(ClassNotFoundException e) {}
            
            if(proxyClass == null) {
                CtClass ctClass = pool.makeClass(lazyProxyName);
                if(Modifier.isInterface(type.getModifiers())) {
                    ctClass.addInterface(ctParent);
                } else {
                    ctClass.setSuperclass(ctParent);
                }
                CtField ctField0 = CtField.make("private " + type.getName() + " delegate;", ctClass);
                ctClass.addField(ctField0);
                CtField ctField1 = CtField.make("private " + Supplier.class.getName() + " supplier;", ctClass);
                ctClass.addField(ctField1);
                
                CtConstructor ctConstructor = CtNewConstructor.make(new CtClass[]{pool.get(Supplier.class.getName())}, new CtClass[0], ctClass);
                ctConstructor.setBody("{this.supplier=$1;}");
                ctClass.addConstructor(ctConstructor);
                
                List<CtMethod> ctMethods = getProxyMethods(ctParent);
                for (CtMethod ctMethod : ctMethods) {
                    StringBuilder body = new StringBuilder()
                        .append("{if(this.delegate==null) {this.delegate=(").append(type.getName()).append(")this.supplier.get();}")
                        .append(ctMethod.getReturnType() == CtClass.voidType ? "" : "return ")
                        .append("this.delegate.").append(ctMethod.getName()).append("(");
                    for (int i = 0; i < ctMethod.getParameterTypes().length; i++) body.append((i==0?"":",")).append("$").append(i+1);
                    body.append(");}");
                    CtMethod ctnMethod = CtNewMethod.copy(ctMethod, ctClass, null);
                    ctnMethod.setBody(body.toString());
                    ctClass.addMethod(ctnMethod);
                }
                proxyClass = ctClass.toClass();
            }
            return (T) proxyClass.getConstructor(Supplier.class).newInstance(supplier);
        } catch (NotFoundException | RuntimeException | CannotCompileException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    public static void setDelegate(Object bean, Object delegate) {
        try {
            Field field = bean.getClass().getDeclaredField("delegate");
            field.setAccessible(true);
            field.set(bean, delegate);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    private static List<CtMethod> getProxyMethods(CtClass parent) throws NotFoundException {
        Map<String, CtMethod> hash = new HashMap<>();
        getProxyMethods(hash, parent.getName(), parent, new HashSet<>());
        return new ArrayList<>(hash.values());
    }
    
    private static void getProxyMethods(Map<String, CtMethod> hash, String from, CtClass clazz, Set<CtClass> visitedClasses) throws NotFoundException {
        if ("java.lang.Object".equals(clazz.getName())) return;
        if (!visitedClasses.add(clazz)) return;

        CtClass[] ifs = clazz.getInterfaces();
        for (int i = 0; i < ifs.length; i++)
            getProxyMethods(hash, from, ifs[i], visitedClasses);

        CtClass parent = clazz.getSuperclass();
        if (parent != null)
            getProxyMethods(hash, from, parent, visitedClasses);

        clazz.defrost();
        CtMethod[] methods = clazz.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            if (isVisible(methods[i].getModifiers(), from, methods[i])) {
                CtMethod m = methods[i];
                String key = m.getName() + ':' + m.getMethodInfo().getDescriptor();
                CtMethod oldMethod = (CtMethod)hash.put(key, methods[i]); 
                if (null != oldMethod && Modifier.isPublic(oldMethod.getModifiers()) && !Modifier.isPublic(methods[i].getModifiers()) ) {//取public的方法
                    hash.put(key, oldMethod); 
                }
            }
        }
    }
    
    private static boolean isVisible(int mod, String from, CtMember meth) {
        if ((mod & (Modifier.PRIVATE | Modifier.STATIC)) != 0) {
            return false;
        } else if ((mod & (Modifier.PUBLIC | Modifier.PROTECTED)) != 0) {
            return true;
        } else {
            String p = getPackageName(from);
            String q = getPackageName(meth.getDeclaringClass().getName());
            return p == null ? q == null : p.equals(q);
        }
    }

    private static String getPackageName(String name) {
        int i = name.lastIndexOf('.');
        return i < 0 ? null : name.substring(0, i);
    }

}
