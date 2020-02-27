package com.x.modular;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


//inject bridge method
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
public @interface ModularBridge {
	
    Type value() default Type.BRIDGE; //默认为用来识别泛型
    
    public static enum Type {
        ENTRY,       //用来增加参数的方法, 除去@ModularInject之外的参数 按原@ModularBridge方法的参数以顺序一一对应
        BRIDGE,      //需要注入的方法   
        INTERCEPT,   //拦截的方法(around bridge), 按Bridge增加参数之后调用该方法, 可以没有
    }

}
