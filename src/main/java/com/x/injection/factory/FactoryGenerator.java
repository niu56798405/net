package com.x.injection.factory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;


/**
 * 
 * 生成@Factory对应的实例
 * @author 
 *
 */
public class FactoryGenerator {
    
    public synchronized static Object generate(Class<?> factoryInteface, List<Class<?>> classes) {
        Factory anno = factoryInteface.getAnnotation(Factory.class);
        Class<? extends Annotation> type = anno.value();
        List<Class<?>> elements = new ArrayList<Class<?>>();
        for (Class<?> clazz : classes) {
            if(clazz.getAnnotation(type) != null) {
                elements.add(clazz);
            }
        }
        
        return generateUseSwitchCase(factoryInteface, type, elements) ;
    }

    private static Object generateUseSwitchCase(Class<?> factoryInteface, Class<? extends Annotation> type, List<Class<?>> elements) {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctParent = pool.getCtClass(factoryInteface.getName());
            CtClass ct = pool.makeClass(factoryInteface.getName() + "$$Proxy");
            ct.addInterface(ctParent);
            
            Method method = type.getDeclaredMethods()[0];//annoation has one method
            
            CtMethod[] cms = ctParent.getDeclaredMethods();
            for (CtMethod cm : cms) {
                if(cm.getParameterTypes().length == 0) continue;
                StringBuilder params = new StringBuilder();
                for (int i = 1; i < cm.getParameterTypes().length; i++) {
                    params.append((i == 1 ? "" : ",")).append("$").append(i + 1);
                }
                
                CtMethod ctMethod = CtNewMethod.copy(cm, ct, null);
                StringBuilder body = new StringBuilder().append("switch($1) {");
                for (Class<?> clazz : elements) {
                    Annotation anno = clazz.getAnnotation(type);
                    if(anno != null) {
                        body.append("case ").append(method.invoke(anno)).append(": return new ").append(clazz.getName()).append("(").append(params).append(");");
                    }
                }
                
                Factory anno = factoryInteface.getAnnotation(Factory.class);
                if(Class.class.equals(anno.defaultType())) {
                    body.append("default: return null;}");
                } else {
                    body.append("default: return new ").append(anno.defaultType().getName()).append("(").append(params).append(");}");
                }
                
                ctMethod.setBody(body.toString());
                ct.addMethod(ctMethod);
            }
            
            return (ct.toClass().newInstance());
        } catch (InstantiationException | IllegalAccessException | NotFoundException | RuntimeException | CannotCompileException | InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
