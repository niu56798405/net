package com.x.injection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 
 * 显示标注bean之间的依赖关系
 * 某些Bean对Prototype的依赖暂时无法通过静态信息分析出来
 * @author 
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface InjectDependence {

    Class<?>[] value() default {};
    
}
