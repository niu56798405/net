package com.x.http.service;

public interface ServiceConfiguration {
    
    ServiceErrorHandler getErrorhandler();

    RequestInteceptor getInteceptor();
    
    FileHandler getFileHandler();

}
