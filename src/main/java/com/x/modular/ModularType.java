package com.x.modular;

import java.util.concurrent.TimeUnit;


/**
 * 
 * 模块类型
 * 暂时只需要,长驻/非长驻内存类型
 * @author 
 *
 */
public enum ModularType {
	
	RESIDENT (TimeUnit.MINUTES.toMillis(60)),
	
	TRANSIENT (TimeUnit.MINUTES.toMillis(15));
	
	public final long unloadIdleTime;
	
    private ModularType(long time) {
        this.unloadIdleTime = time;
    }	
	
}
