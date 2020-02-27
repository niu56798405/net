package com.x.rpc.server;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.x.injection.ApplicationContext.Finalizer;
import com.x.injection.Bean;
import com.x.rpc.RpcService;
import com.x.tools.Log;

@Bean
public class RpcServiceContainer implements Finalizer{
	private Map<String,  RpcBean> serviceBeans = new HashMap<>();

	@Override
	public void finalize(List<Class<?>> clazzes) {
		clazzes.forEach(this::registService);
    }
    
    public void registService(Class<?> clazz) {
    	RpcService ann = clazz.getAnnotation(RpcService.class);
        if(ann != null && !Modifier.isAbstract(clazz.getModifiers()) && !Modifier.isInterface(clazz.getModifiers())) {
        	try{
		    	Class<?> superclass = clazz.getInterfaces()[0];
		        String name = superclass.getSimpleName();
		        RpcBean bean = new RpcBean();
		        Object obj = clazz.newInstance();
		        Map<String, RpcMethod> methods = bean.getMethods();
		        Method[] methodsTemp = clazz.getMethods();
		        for(Method method : methodsTemp){
		        	if (!RpcMethod.check(method)) {   
		        		 continue; 
		        	}
		        	Class<?>[] parameterTypes = method.getParameterTypes();
		        	StringBuilder mName = new StringBuilder(method.getName());
		        	if (parameterTypes != null) {
						for(Class<?> type : parameterTypes){
				        	mName.append(type.getSimpleName());
						}
					}
		        	RpcMethod rpcMethod = MethodProxyBuilder.build(obj, method);		        	
		        	methods.put(mName.toString(), rpcMethod);
		        }
		        
		        serviceBeans.put(name, bean);
        	}catch(Exception e){
        		e.printStackTrace();
        	} 
        }
    }
	
	public RpcBean getRpcBean(String beanName){
		return serviceBeans.get(beanName);
	}
	
	@SuppressWarnings("finally")
	public Object invoke(String beanName, String mName, Object[] params){
		Object obj = null;
		try {
		
			obj = getRpcBean(beanName).invoke(mName, params); 
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			return obj;
		}
	}
	
	public int getSize(){
		return serviceBeans.size();
	}
	
}
