package com.x.http.service;

public interface ServiceConflictHandler {
	
	public void handle(String path, Service s1, Service s2);

}
