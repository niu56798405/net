package com.x.tools;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.TypeUtils;


public class JsonUtil {
			
	public static Object castToJavaBean(String json, Class<?> clazz, Type genericParameterType){
		if (Map.class.isAssignableFrom(clazz) || Collection.class.isAssignableFrom(clazz)) {
			return JSON.parseObject(json, genericParameterType);
		}else{
			return TypeUtils.castToJavaBean(json, clazz);	
		}
	}	
}
