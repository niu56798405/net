package com.x.http.service;

public interface ServiceErrorHandler {
    
    public Response handle(Request req, Throwable e);
    
}
