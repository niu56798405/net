package com.x.rpc.server;

import java.util.HashMap;
import java.util.Map;

public class RpcBean {
	private Map<String, RpcMethod> methods = new HashMap<>();
	
	
	
	public Object invoke(String methodName, Object[] params)throws Exception {
		return methods.get(methodName).invoke(params);
	}


	public Map<String, RpcMethod> getMethods() {
		return methods;
	}
	
	public RpcMethod getMethod(String mName){
		return methods.get(mName);
	}
	
}
