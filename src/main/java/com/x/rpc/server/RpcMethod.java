package com.x.rpc.server;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public interface RpcMethod {


	public Object invoke(Object[] params);
	
			
	public static boolean check(Method method) {
		String mName = method.getName();
		  if (mName.equals("toString")|| mName.equals("hashCode")|| mName.equals("equals")||
          		Modifier.isStatic(method.getModifiers())||
          		Modifier.isNative(method.getModifiers()) ||
          		Modifier.isFinal(method.getModifiers())) {
              return false;
          }
		  return true;
	}
		
}
