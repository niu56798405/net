package com.x.rpc.client;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.x.injection.ApplicationContext.Finalizer;
import com.x.injection.Bean;
import com.x.injection.Inject;
import com.x.rpc.RpcInterface;
import com.x.rpc.server.RpcMethod;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
@SuppressWarnings("all")
@Bean
public class RpcProxy implements Finalizer{

    
    protected AtomicInteger counter = new AtomicInteger();
    @Inject
    private RpcInvoker invoker;
    
    private Map<Class<?>, Object> cache = new HashMap<>();

      
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> interfaceClass) {
		return  (T) cache.get(interfaceClass);   
    }

    
    private void load(Class<?> interfaceClass) {
    	try {
    		String interfaceName = interfaceClass.getSimpleName();
     	   	ClassPool mPool = ClassPool.getDefault();
            CtClass mCtc = mPool.makeClass(interfaceName + "_proxy_" + counter.getAndIncrement());
            if (interfaceClass.isInterface()) {
                mCtc.addInterface(mPool.getCtClass(interfaceClass.getName()));
            } else {
                throw new IllegalArgumentException(interfaceClass.getName() + " is not an interface");
            }
            List<String> fieldList = new ArrayList<String>();
            List<String> methodList = new ArrayList<String>();

            fieldList.add("public " + RpcInvoker.class.getCanonicalName() + " proxyInvoker = null;");
            createMethod(interfaceClass, fieldList, methodList);

            for (String fieldStr : fieldList) {
          
                mCtc.addField(CtField.make(fieldStr, mCtc));
            }
            for (String methodStr : methodList) {
            
                mCtc.addMethod(CtMethod.make(methodStr, mCtc));
            }
    
            Class<?> clazz = mCtc.toClass();
            mCtc.detach();//从 classPool释放
            Object instance = clazz.newInstance();
            clazz.getField("proxyInvoker").set(instance, invoker);           
            cache.put(interfaceClass, instance);
                            		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createMethod(Class<?> interfaceClass, List<String> fieldList, List<String> methodList) {	   
		String interfaceName = interfaceClass.getSimpleName();
		Method[] methodAry = interfaceClass.getMethods();
	   
        for (Method m : methodAry) {
    	    StringBuilder sb = new StringBuilder(512);

            if (!RpcMethod.check(m)) {
                continue;
            }
            Class<?> returnType = m.getReturnType();
            Class<?>[] parameterTypes = m.getParameterTypes();
//            Type genericReturnType = m.getGenericReturnType();
			Class<?>[] genericReturnClasses;
//			if (Map.class.isAssignableFrom(returnType) || Collection.class.isAssignableFrom(returnType)) {
//				ParameterizedTypeImpl type = (ParameterizedTypeImpl)genericReturnType;
//				Type[] actualTypeArguments = type.getActualTypeArguments();
//				genericReturnClasses = new Class[actualTypeArguments.length];
//				int i = 0;
//				for(Type gType : actualTypeArguments){
//					genericReturnClasses[i] = (Class<?>)gType; 
//					i++;		
//				}
//			}else {
//				genericReturnClasses = new Class[0];
//			}
        
            StringBuilder methodName = new StringBuilder(m.getName());
            sb.append("public " + returnType.getName()+" " + methodName + " ( ");
           
            int c = 0;
            if (parameterTypes != null && parameterTypes.length > 0) {
            	for (Class<?> mp : parameterTypes) {
                    sb.append(" " + mp.getCanonicalName() + " arg" + c + " ,");
                    methodName.append(mp.getSimpleName());
                    c++;
                }   
            	sb.deleteCharAt(sb.length() - 1);
			}            
            
            sb.append(" ){");
            sb.append("String interfaceName = \"" + interfaceName +"\";"); 
            sb.append("String methodName = \"" + methodName.toString() + "\";");
//            sb.append("Class[] returnGenericType = new Class[ "+ genericReturnClasses.length +" ];");
//            for (int i = 0; i < genericReturnClasses.length; i++) {
//				sb.append("returnGenericType[" + i + "] = " + genericReturnClasses[i].getName() + ".class;");
//			}	
			
         
            sb.append(" Object[] paramValues = new Object[" + c + "];");        	
            for (int i = 0; i < c; i++) {
                sb.append("paramValues[" + i + "] = ($w)$" + (i + 1) + ";");              
            }
            sb.append("Class returnType = " + returnType.getCanonicalName()+ ".class" + " ;");
            StringBuilder invokeStr = new StringBuilder();
//            invokeStr.append(" proxyInvoker.invoke(" +"new " + RpcRequest.class.getCanonicalName() 
//            		+"(interfaceName, methodName , paramValues), returnType, returnGenericType);");
            invokeStr.append(" proxyInvoker.invoke(" +"new " + RpcRequest.class.getCanonicalName() 
            		+"(interfaceName, methodName , paramValues), returnType);");
          
            if (returnType != void.class) {
            	sb.append("Object a = ");
            	sb.append(invokeStr);
            	sb.append("return "+ asArgument(returnType, "a") +";");
			}else{
				sb.append(invokeStr);
			}                     
            sb.append("}");             
            methodList.add(sb.toString());
           
       }
	} 
	
    private String asArgument(Class<?> cl, String name) {
        if (cl.isPrimitive()) {
            if (Boolean.TYPE == cl) {
                return name + "==null?false:((Boolean)" + name + ").booleanValue()";
            }
            if (Byte.TYPE == cl) {
                return name + "==null?(byte)0:((Byte)" + name + ").byteValue()";
            }
            if (Character.TYPE == cl) {
                return name + "==null?(char)0:((Character)" + name + ").charValue()";
            }
            if (Double.TYPE == cl) {
                return name + "==null?(double)0:((Double)" + name + ").doubleValue()";
            }
            if (Float.TYPE == cl) {
                return name + "==null?(float)0:((Float)" + name + ").floatValue()";
            }
            if (Integer.TYPE == cl) {
                return name + "==null?(int)0:((Integer)" + name + ").intValue()";
            }
            if (Long.TYPE == cl) {
                return name + "==null?(long)0:((Long)" + name + ").longValue()";
            }
            if (Short.TYPE == cl) {
                return name + "==null?(short)0:((Short)" + name + ").shortValue()";
            }
            throw new RuntimeException(name + " is unknown primitive type.");
        }
        return "(" + cl.getName() + ")" + name;
    }
	



	@Override
	public void finalize(List<Class<?>> clazzes) {
		for(Class<?>clazz : clazzes){
			RpcInterface annotation = clazz.getAnnotation(RpcInterface.class);
			if (annotation != null) {				
				load(clazz);
			}
		}		
	}	
}