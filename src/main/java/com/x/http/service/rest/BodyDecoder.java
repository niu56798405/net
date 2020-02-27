package com.x.http.service.rest;

public interface BodyDecoder {
	
	public Object decode(Class<?> type, byte[] body);

}
