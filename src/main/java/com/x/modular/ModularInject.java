package com.x.modular;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要注入的属性/参数/...etc
 * Parameter仅支持Module注入
 * Field仅支持Module.share()注入
 * 其他情况注入需要直接写代码描述(例: this.xxx, $1(方法中第一个参数))
 * @author 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface ModularInject {
	
    boolean combine() default false;//注入到某个类型接口的字段中, 由实现了该接口的多个模块组合而来, 接口中返回值必须为void
    
}
