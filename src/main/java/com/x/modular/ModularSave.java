package com.x.modular;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * player.save()时调用的方法
 * 参数只能从player的 protected和public属性或方法 中获取
 * @author 
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ModularSave {

    String[] value() default {};
    
}
