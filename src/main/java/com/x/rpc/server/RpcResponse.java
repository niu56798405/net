package com.x.rpc.server;

public class RpcResponse {
	private long reqCode;
	private Object result;
	
	
	public RpcResponse(long reqCode, Object result) {
		super();
		this.reqCode = reqCode;
		this.result = result;
	}
	
	
	public RpcResponse() {
		super();
	}

	public long getReqCode() {
		return reqCode;
	}
	public void setReqCode(long reqCode) {
		this.reqCode = reqCode;
	}
	public Object getResult() {
		return result;
	}
	
	public void setResult(Object result) {
		this.result = result;
	}
	
}
