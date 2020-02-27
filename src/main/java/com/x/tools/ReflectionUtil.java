package com.x.tools;

import java.lang.reflect.ParameterizedType;

public class ReflectionUtil extends SecurityManager {
    static final ReflectionUtil reflection = new ReflectionUtil();

    protected Class<?>[] getClassContext() {
        return super.getClassContext();
    }

    public static Class<?> getCallerClass() {
        return getCallerClass(2);
    }

    public static Class<?> getCallerClass(int depth) {
        Class[] classes = reflection.getClassContext();
        int index = depth + 2;
        int len = classes.length;
        return len > index ? classes[index] : classes[len - 1];
    }

    public static Class<?> getCallerClass(Class<?> anchor) {
        boolean next = false;
        Class[] arg1 = reflection.getClassContext();
        int arg2 = arg1.length;

        for (int arg3 = 0; arg3 < arg2; ++arg3) {
            Class clazz = arg1[arg3];
            if (anchor.equals(clazz)) {
                next = true;
            } else if (next) {
                return clazz;
            }
        }

        return Object.class;
    }
    /**
     * 获取 类的泛型 class
     * @param clazz
     * @return
     */
    public static <T>Class<T> getGenericClass(Class<T> clazz){
    	ParameterizedType parameterizedType = (ParameterizedType) clazz.getGenericSuperclass();
        @SuppressWarnings("unchecked")
		Class<T> type = (Class<T>) parameterizedType.getActualTypeArguments()[0];
        return type;
    }
    
}