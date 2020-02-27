package com.x.injection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Bean {
    
    /**
     * 优先加载
     * @return
     */
    public boolean prior() default false;
    
    /**
     * 作为备选方案实现,如果没有发现其他实现该类(或该类的接口)的bean, 则使用该类
     * @return
     */
    public boolean backup() default false;
    
}
