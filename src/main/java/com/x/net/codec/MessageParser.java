package com.x.net.codec;

import java.util.HashMap;
import java.util.Map;

import com.x.injection.Bean;
import com.x.tools.ReflectionUtil;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

/**
 * 
 *
 * @author niu
 *
 */
@Bean
public class MessageParser {
	
    private Map<Class<?>, Parser<?>> map = new HashMap<>();
    
    @SuppressWarnings("unchecked")
	public <T> T parseFrom(Class<T> clazz, byte[] array) throws Exception {
    	Parser<?> parser = map.get(clazz);
        if (parser == null) {        	
            Class<T> type = ReflectionUtil.getGenericClass(clazz);   
            parser = buildParser(type);
            map.put(clazz, parser);
        }
        return (T) parser.parse(array);
   }
    
    @SuppressWarnings("unchecked")
	public <T>Parser<T> buildParser(Class<T> clazz) throws Exception{
				
		String interfaceName = clazz.getCanonicalName();
 	   	ClassPool mPool = ClassPool.getDefault();
        CtClass mCtc = mPool.makeClass(interfaceName + "_proxy_" + "Parser");
        
        mCtc.addInterface(mPool.getCtClass(Parser.class.getName()));        
        StringBuilder sb = new StringBuilder();
        sb.append("public Object parse(byte[] array) {");
        sb.append("return " + interfaceName + ".parseFrom(array);");
        sb.append("}");
   
        CtMethod make = CtMethod.make(sb.toString(), mCtc);
		mCtc.addMethod(make);
        
      
        Class<?> proxyClass = mCtc.toClass();
        mCtc.detach();
        Object instance = proxyClass.newInstance();					
		return (Parser<T>) instance;
	}
	
	public interface Parser<T>{
		@SuppressWarnings("hiding")
		public <T> T parse(byte[] array);
	}
		 
   
}
