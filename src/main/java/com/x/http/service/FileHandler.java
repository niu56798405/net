package com.x.http.service;

/**
 * 
 * 文件处理(下载)
 * @author 
 *
 */
public interface FileHandler {
    
    /**
     * @param path
     * @return null if not exists
     */
    public String getPath(String path);

}
