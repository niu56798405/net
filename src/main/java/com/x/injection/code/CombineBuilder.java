package com.x.injection.code;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.x.injection.Combine;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

public class CombineBuilder {
    
    public static final String APPEND_COMBINE_DELEGATE_METHOD = "appendCombineDelegate";
    public static final String REMOVE_COMBINE_DELEGATE_METHOD = "removeCombineDelegate";
    
    public static void combine(Object combine, Object bean) {
        try {
            Method method = combine.getClass().getMethod(APPEND_COMBINE_DELEGATE_METHOD, Object.class);
            method.invoke(combine, bean);
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            //ignore
        }
    }

    public static Object buildBean(Class<?> combineClazz) {
        try {
            return buildClass(combineClazz).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public synchronized static Class<?> buildClass(Class<?> combineClazz) {
        try {
            Combine combine = combineClazz.getAnnotation(Combine.class);

            Class<?> type = combineClazz;
            ClassPool pool = ClassPool.getDefault();
            CtClass ctParent = pool.get(type.getName());
            String proxyName = type.getName() + "$$CombineProxy";

            Class<?> proxyClass = null;
            try {
                proxyClass = Class.forName(proxyName);
            } catch(ClassNotFoundException e) {}

            if(proxyClass == null) {
                CtClass ctClass = pool.makeClass(proxyName);
                if(Modifier.isInterface(type.getModifiers())) {
                    ctClass.addInterface(ctParent);
                } else {
                    ctClass.setSuperclass(ctParent);
                }

                CtField ctField0 = CtField.make("private final java.util.LinkedList delegates = new java.util.LinkedList();", ctClass);
                ctClass.addField(ctField0);

                CtMethod appendDelegateMethod = CtMethod.make("public void " + APPEND_COMBINE_DELEGATE_METHOD + "(java.lang.Object bean)" + (combine.offerFirst() ? "{this.delegates.addFirst(bean);}" : "{this.delegates.add(bean);}"), ctClass);
                ctClass.addMethod(appendDelegateMethod);
                
                CtMethod removeDelegateMethod = CtMethod.make("public void " + REMOVE_COMBINE_DELEGATE_METHOD + "(java.lang.Object bean) {this.delegates.remove(bean);}", ctClass);
                ctClass.addMethod(removeDelegateMethod);

                CtMethod[] ctMethods = ctParent.getMethods();
                for (CtMethod ctMethod : ctMethods) {
                    if(ctMethod.getDeclaringClass().getName().equals("java.lang.Object")) continue;
                    if(Modifier.isStatic(ctMethod.getModifiers())) continue;
                    StringBuilder body = new StringBuilder("{");
                    body.append("java.util.Iterator it = this.delegates.iterator();")
                    .append("while(it.hasNext()){")
                    .append(combineClazz.getName()).append(" delegate = ").append("(").append(combineClazz.getName()).append(") it.next();");
                    if(ctMethod.getReturnType() == CtClass.voidType) {
                        appendInvokeMethod(ctMethod, body);
                        body.append("}");//结束while
                    } else if(ctMethod.getReturnType() == CtClass.booleanType) {
                        body.append("boolean r = ");
                        appendInvokeMethod(ctMethod, body);
                        body.append(combine.boolByTrue() ? "if(r) {return true;}" : "if(!r) {return false;}")
                        .append("}")//结束while
                        .append("return ").append(combine.boolByTrue() ? "false" : "true").append(";");
                    } else {
                        body.append(ctMethod.getReturnType().getName()).append(" r = ");
                        appendInvokeMethod(ctMethod, body);
                        body.append("if(r != null) {return r;}")
                        .append("}")//结束while
                        .append("return null;");
                    }
                    body.append("}");
                    CtMethod ctnMethod = CtNewMethod.copy(ctMethod, ctClass, null);
                    ctnMethod.setBody(body.toString());
                    ctClass.addMethod(ctnMethod);
                }
                proxyClass = ctClass.toClass();
            }
            return proxyClass;
        } catch (NotFoundException | RuntimeException | CannotCompileException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static void appendInvokeMethod(CtMethod ctMethod, StringBuilder body) throws NotFoundException {
        body.append("delegate.").append(ctMethod.getName()).append("(");
        for (int i = 0; i < ctMethod.getParameterTypes().length; i++) body.append((i==0?"":",")).append("$").append(i+1);
        body.append(");");
    }
    
}
