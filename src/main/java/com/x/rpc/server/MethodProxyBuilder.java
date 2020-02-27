package com.x.rpc.server;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;

public class MethodProxyBuilder {
	private static AtomicInteger index = new AtomicInteger();
	 
	public static RpcMethod build(Object object, Method method)throws Exception{
		String className = object.getClass().getName();
		String methodName = method.getName();
		Class<?> returnType = method.getReturnType();
		Class<?>[] parameterTypes = method.getParameterTypes();
	   	ClassPool mPool = ClassPool.getDefault();
        CtClass mCtc = mPool.makeClass(className + "Proxy" + methodName + index.incrementAndGet()); 
        mCtc.addInterface(mPool.getCtClass(RpcMethod.class.getCanonicalName()));
        
        List<String> methodList = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        sb.append("public Object invoke(Object[] objs){");
        
        if (parameterTypes == null || parameterTypes.length == 0) {
			if (returnType != void.class) {
				sb.append("return  ($w)");
			}
        	sb.append("object." + methodName + "();");
        	if (returnType == void.class) {
				sb.append("return null;");
			}			
		}else {
			if (returnType != void.class) {
				sb.append("return ($w)");
			}
        	sb.append("object." + methodName + "(");
        	for(int i=0;i< parameterTypes.length; i++){
        		Class<?> clazz = parameterTypes[i];        		
        		sb.append(castType(clazz, "objs[" + i +"]"));
        		if (i + 1 < parameterTypes.length) {
        			sb.append(",");	
				}        	
        	}
        	sb.append(" );");
        	
        	if (returnType == void.class) {
				sb.append("return null;");
			}			
		}
                     
        sb.append("}");
        methodList.add(sb.toString());
        mCtc.addField(CtField.make("public " + object.getClass().getCanonicalName()+ " object = null;", mCtc));
        
        for (String methodStr : methodList) {       
            mCtc.addMethod(CtMethod.make(methodStr, mCtc));
        }

        Class<?> clazz = mCtc.toClass();
        Object o = clazz.getConstructor().newInstance();
        RpcMethod instance = (RpcMethod)o;
        mCtc.detach();//从 classPool释放
        
        clazz.getField("object").set(instance, object);           
        return instance;
	}
	
    private static String castType(Class<?> cl, String name) {
        if (boolean.class == cl) {
            return "((Boolean)" + name + ").booleanValue()";
        }
        if (byte.class == cl) {
            return "((Byte)" + name + ").byteValue()";
        }
        if (char.class == cl) {
            return "((Character)" + name + ").charValue()";
        }
        if (double.class == cl) {
            return "((Double)" + name + ").doubleValue()";
        }
        if (float.class == cl) {
            return "((Float)" + name + ").floatValue()";
        }
        if (int.class == cl) {
            return "((Integer)" + name + ").intValue()";
        }
        if (long.class == cl) {
            return "((Long)" + name + ").longValue()";
        }
        if (short.class == cl) {
            return name + "==null?(short)0:((Short)" + name + ").shortValue()";
        }
        return "(" + cl.getName() + ")" + name;
    }
	
	
}
