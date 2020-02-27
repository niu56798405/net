package com.x.tools;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;


/**
 * 
 * 泛型分析 根据实现类找出对应父类的泛型实际类型
 * @author 
 *
 */
public class Generic {
    
    private static String keyName(Class<?> clazz, TypeVariable<?> variable) {
        return clazz.getName() + '@' + variable.getName();
    }
    
    /**
     * 获得方法的参数准确类型
     * @param genericInfos
     * @param method
     * @return
     */
    private static Class<?>[] getParamterTypes(GenericInfos genericInfos, Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Class<?>[] ret = new Class<?>[genericParameterTypes.length];
        for (int i = 0; i < ret.length; i++) {
            Type type = genericParameterTypes[i];
            if(type instanceof TypeVariable) {
                TypeVariable<?> tv = (TypeVariable<?>) type;
                Class<?> clazz = genericInfos.getByGenericName(tv.getName());
                if(clazz != null) {
                    ret[i] = clazz;
                    continue;
                }
            }
            ret[i] = parameterTypes[i];
        }
        return ret;
    }
    
    public static GenericInfos analyse(Class<?> type, Class<?> genericClass) {
        if(!genericClass.isAssignableFrom(type)) return new GenericInfos();
        
        Map<String, Class<?>> genericInfos = new HashMap<String, Class<?>>();
        do {
            Type generic = type.getGenericSuperclass();
            if(generic instanceof ParameterizedType) {
                ParameterizedType genericType = (ParameterizedType) generic;
                type = (Class<?>) genericType.getRawType();
                
                TypeVariable<?>[] typeParameters = type.getTypeParameters();
                Type[] typeArguments = genericType.getActualTypeArguments();
                assert typeParameters.length == typeArguments.length;
                
                for (int i = 0; i < typeArguments.length; i++) {
                    Type typeArgument = typeArguments[i];
                    if(typeArgument instanceof Class<?>) {
                        genericInfos.put(keyName(type, typeParameters[i]), (Class<?>) typeArguments[i]);
                    } else {
                        TypeVariable<?> variable = (TypeVariable<?>) typeArgument;
                        genericInfos.put(keyName(type, typeParameters[i]), genericInfos.get(keyName((Class<?>) variable.getGenericDeclaration(), variable)));
                    }
                }
            } else {
                type = (Class<?>) generic;
            }
        } while(!type.equals(genericClass));
        
        TypeVariable<?>[] typeParameters = genericClass.getTypeParameters();
        GenericInfo[] ret = new GenericInfo[typeParameters.length];
        for (int i = 0; i < typeParameters.length; i++) {
            ret[i] = new GenericInfo(typeParameters[i].getName(), genericInfos.get(keyName(genericClass, typeParameters[i])));
        }
        return new GenericInfos(ret);
    }
    
    public static class GenericInfos {
        public final GenericInfo[] infos;
        public Class<?> getByGenericName(String name) {
            for (GenericInfo info : infos) {
                if(info.name.equals(name)) return info.type;
            }
            return null;
        }
        public Class<?>[] getParameterTypes(Method method) {
            return getParamterTypes(this, method);
        }
        
        public GenericInfos() {
            this(new GenericInfo[0]);
        }
        public GenericInfos(GenericInfo[] infos) {
            this.infos = infos;
        }
    }
    
    public static class GenericInfo {
        public final String name;
        public final Class<?> type;
        public GenericInfo(String name, Class<?> type) {
            this.name = name;
            this.type = type;
        }
        @Override
        public String toString() {
            return name + " : " + type.getName();
        }
    }

}
