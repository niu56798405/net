package com.x.modular;

/**
 * 
 * 模块
 * 
 * @author 
 *
 * @param <T> sharable interface
 */
public interface Module<T> {
    
    T share();
    
}
