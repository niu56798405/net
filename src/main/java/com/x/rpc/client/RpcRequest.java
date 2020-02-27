package com.x.rpc.client;

import java.util.Arrays;

public class RpcRequest {
	private Object[] params;
	private String interfaceName;
	private String methodName;
	private long reqCode;
		

	public RpcRequest( String interfaceName, String methodName , Object[] params) {
		super();		
		this.params = params;
		this.interfaceName = interfaceName;
		this.methodName = methodName;
		
	}
	
	
	
	public RpcRequest() {
		super();
		
	}


	public long getReqCode() {
		return reqCode;
	}



	public void setReqCode(long reqCode) {
		this.reqCode = reqCode;
	}
	
	public Object[] getParams() {
		return params;
	}
	public void setParams(Object[] params) {
		this.params = params;
	}
	public String getInterfaceName() {
		return interfaceName;
	}
	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}



	@Override
	public String toString() {
		return "RpcRequest [params=" + Arrays.toString(params) + ", interfaceName=" + interfaceName + ", methodName="
				+ methodName + ", reqCode=" + reqCode + "]";
	}
	
	
}
