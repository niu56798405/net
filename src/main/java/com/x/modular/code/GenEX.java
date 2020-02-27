package com.x.modular.code;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;

import com.x.modular.ModularBridge;
import com.x.modular.ModularBridge.Type;
import com.x.modular.ModularEnigne;
import com.x.modular.ModularInject;
import com.x.modular.ModuleContainer;
import com.x.modular.ModuleTypeLoader;
import com.x.tools.Generic;

/**
 * generate modular bridge clazz
 * @author 
 */
public class GenEX {
    
    private static String LOADER_NAME = "_moduleLoader";

    public static Object generate(Class<?> clazz, Class<?> parentClazz) {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctClazz = pool.get(clazz.getName());

            //Override method
            //bridge method must override inject method or is the same method
            CtMethod ovvrideMethod = findModularBridgeMethod(ctClazz, Type.ENTRY);
            CtMethod  bridgeMethod = findModularBridgeMethod(ctClazz, Type.BRIDGE);
            CtMethod  aroundMethod = findModularBridgeMethod(ctClazz, Type.INTERCEPT);
            
            CtMethod    callMethod = aroundMethod == null ? bridgeMethod : aroundMethod;
            
            CtClass proxyClazz = pool.makeClass(clazz.getName() + "$$BridgeProxy");
            proxyClazz.setSuperclass(ctClazz);
            
            CtField ctField = new CtField(pool.get(ModuleTypeLoader.class.getName()), LOADER_NAME, proxyClazz);
            proxyClazz.addField(ctField);
            
            CtMethod proxyMethod = CtNewMethod.copy(ovvrideMethod, proxyClazz, null);
            proxyMethod.setBody(generateInjectCode(callMethod, bridgeMethod, ovvrideMethod));
            proxyClazz.addMethod(proxyMethod);
            
            return makeInstance(proxyClazz, parentClazz);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected static Object makeInstance(CtClass proxyClazz, Class<?> parentClazz) throws Exception {
        Class<?> fclazz = proxyClazz.toClass();
        Object bean = fclazz.newInstance();
        Field field = fclazz.getDeclaredField(LOADER_NAME);
        field.setAccessible(true);
        field.set(bean, ModularEnigne.fetchLoader(findModuleType(fclazz, parentClazz)));
        return bean;
    }
    
    protected static Class<?> findModuleType(Class<?> clazz, Class<?> parentClazz) throws Exception {
        Method method = findModularBridgeMethod(clazz, Type.BRIDGE);
        Class<?>[] types = Generic.analyse(clazz, parentClazz).getParameterTypes(method);
        Annotation[][] annss = method.getParameterAnnotations();
        for (int i = 0; i < annss.length; i++) {
            Annotation[] anns = annss[i];
            for (Annotation ann : anns) {
                if(ann.annotationType().equals(ModularInject.class)) {
                    return types[i];
                }
            }
        }
        return null;
    }
    
    private static Method findModularBridgeMethod(Class<?> clazz, ModularBridge.Type type) throws Exception {
        while(!Object.class.equals(clazz)) {
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                ModularBridge anno = (ModularBridge) method.getAnnotation(ModularBridge.class);
                if(anno != null && anno.value() == type) {
                    return method;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }
    
    
    private static CtMethod findModularBridgeMethod(CtClass clazz, ModularBridge.Type type) throws Exception {
        while(!Object.class.getName().equals(clazz.getName())) {
            CtMethod[] methods = clazz.getDeclaredMethods();
            for (CtMethod method : methods) {
                ModularBridge anno = (ModularBridge) method.getAnnotation(ModularBridge.class);
                if(anno != null && anno.value() == type) {
                    return method;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }
    
    private static String generateInjectCode(CtMethod callMethod, CtMethod injectMethod, CtMethod proxyMethod) throws Exception {
        Object[][] parameterAnnotations = injectMethod.getParameterAnnotations();
        CtClass[] injectParameterTypes = injectMethod.getParameterTypes();
        CtClass[] proxyParameterTypes = proxyMethod.getParameterTypes();
        StringBuilder sb = new StringBuilder("{");
        sb.append(callMethod.getName()).append("(");//call method
        int paramIndex = 0;
        outter:
        for (int i = 0; i < parameterAnnotations.length; i++) {
            sb.append(i == 0 ? "" : ", ");
            Object[] objects = parameterAnnotations[i];
            for (Object object : objects) {
                if(object instanceof ModularInject) {
                    sb.append(LOADER_NAME).append(".load((").append(ModuleContainer.class.getName()).append(")$1)");
                    continue outter;
                }
            }
            if(!proxyParameterTypes[paramIndex].equals(injectParameterTypes[i])) {//如果类型不一样 需要转换
                sb.append("(").append(injectParameterTypes[i].getName()).append(")");
            }
            sb.append("$").append(paramIndex+1);
            ++ paramIndex;
        }
        return sb.append(");}").toString();
    }

    
//    private static CtMethod findGenericBridgeMethod(CtClass clazz, CtMethod method) throws Exception {
//        CtMethod bridgeMethod = clazz.getMethod(method.getName(), method.getSignature());
//        return findGenericBrigdeMethod(clazz, bridgeMethod);
//    }
//    
//    private static CtMethod findGenericBrigdeMethod(CtClass clazz, CtMethod method) throws Exception {
//        if((method.getMethodInfo().getAccessFlags() & AccessFlag.BRIDGE) > 0) {
//            final BridegMethodInfo tmp = new BridegMethodInfo();
//            method.instrument(new ExprEditor(){
//                @Override
//                public void edit(MethodCall m) throws CannotCompileException {
//                    tmp.name = m.getMethodName();
//                    tmp.desc = m.getSignature();
//                }
//            });
//            return clazz.getMethod(tmp.name, tmp.desc);
//        }
//        return null;
//    }
//
//    private static class BridegMethodInfo {
//        public String name;
//        public String desc;
//    }

//    private static String packDescreptor(Method current) {
//        Class<?>[] parameterTypes = current.getParameterTypes();
//        StringBuilder sb = new StringBuilder();
//        sb.append("(");
//        for (Class<?> pt : parameterTypes) {
//            sb.append(packDescreptor(pt));
//        }
//        sb.append(")");
//        Class<?> returnType = current.getReturnType();
//        sb.append(packDescreptor(returnType));
//        return sb.toString();
//    }
//
//    //boolean z, char c, byte b, short s, int i, long j, float f, double d
//    private static String packDescreptor(Class<?> returnType) {
//        if(returnType.equals(boolean.class)) {
//            return "Z";
//        } else if(returnType.equals(char.class)) {
//            return "C";
//        } else if(returnType.equals(byte.class)) {
//            return "B";
//        } else if(returnType.equals(short.class)) {
//            return "S";
//        } else if(returnType.equals(int.class)) {
//            return "I";
//        } else if(returnType.equals(long.class)) {
//            return "J";
//        } else if(returnType.equals(float.class)) {
//            return "F";
//        } else if(returnType.equals(double.class)) {
//            return "D";
//        } else if(returnType.equals(void.class)) {
//            return "V";
//        } else {
//            return "L" + returnType.getName().replaceAll("\\.", "/") + ";";
//        }
//    }
//
//    private static CtMethod findInjectInformationMethod(CtClass clazz, CtMethod method) throws Exception {
//        CtClass superClazz = clazz;
//        while(!hasInjectAnnotation(method)) {//isBridge
//            superClazz = superClazz.getSuperclass();
//            method = superClazz.getMethod(method.getName(), method.getSignature());
//        }
//        return method;
//    }
//
//    private static boolean hasInjectAnnotation(CtMethod method) throws Exception {
//        Object[][] parameterAnnotations = method.getParameterAnnotations();
//        for (Object[] objects : parameterAnnotations) {
//            for (Object object : objects) {
//                if(object instanceof ModularInject) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//    
//    private static CtMethod findExectuableBrigdeMethod(CtClass clazz, CtMethod method) throws Exception {
//        if((method.getMethodInfo().getAccessFlags() & AccessFlag.BRIDGE) > 0) {
//            final BridegMethodInfo tmp = new BridegMethodInfo();
//            method.instrument(new ExprEditor(){
//                @Override
//                public void edit(MethodCall m) throws CannotCompileException {
//                    tmp.name = m.getMethodName();
//                    tmp.desc = m.getSignature();
//                }
//            });
//            return clazz.getMethod(tmp.name, tmp.desc);
//        }
//        return null;
//    }
    
}
