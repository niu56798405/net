package com.x.tools;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.conn.ssl.PrivateKeyStrategy;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
/**
 * <p>注意 目前支持  * + / - >> <<  & | > < % ! 三目运算符   以及 java.lang.Math 中的部分 方法  <P>
 * <p>请勿使用 java.lang.Math 类中的方法名 作为变量名<p> 
 * <p>变量命名须符合 java命名规范，不可使用数字开头 error demo eg： 99Level  <p> 
 * 
 * >> << & | 请使用 () 括起来
 * 
 * <p> eg: ((level | 1) + 100 + vipLevel + (exp >> 10)) % 10 > 99 ? 10 : 20 <p>
 * <p> eg: Math.ceil(level * 25.5) <p>
 * 
 * @author niu
 *
 */
public class ExprUtil {
	//过滤 变量
    private static String regex = "[a-zA-Z_]+[a-zA-Z0-9_]*";
    //过滤 >> << | & 操作 将变量转为 int
    private static String regex1 = "[\\s]*([<>]{2}|[&|]{1})";
   
    private static AtomicLong id = new AtomicLong(1);
    
	public static Expr buildExpr(String exprStr) throws Exception{
       
        Pattern pattern = Pattern.compile(regex);  
        Matcher ma = pattern.matcher(exprStr);  
        List<String> splitArray = new LinkedList<>();
        
        Set<String> mNames = new HashSet<>();
        for(Method m : Math.class.getDeclaredMethods()){
        	mNames.add(m.getName());
        }
                
        while(ma.find()){  
        	String group = ma.group();
        	if ("Math".equals(group) || mNames.contains(group)) {
        		Log.debug("please check exprString have use java.lang.Math methodName as fieldName. exprString {}", exprStr);
        		continue;
        	}  	
			splitArray.add(group);  
        } 
		if (exprStr.contains("exp")) {
			splitArray.add("exp");
		}
        
        
		String interfaceName = Expr.class.getCanonicalName();
 	   	ClassPool mPool = ClassPool.getDefault();
        CtClass mCtc = mPool.makeClass(interfaceName + "Proxy_"+ id.getAndIncrement());      
        mCtc.addInterface(mPool.getCtClass(Expr.class.getName())); 
         
        
        StringBuilder sb = new StringBuilder();
        sb.append("public Object eval(java.util.Map map) {");
        
        for (int i = 0; i < splitArray.size(); i++) {       	        	
        	String string = splitArray.get(i);
          		
        	Pattern p = Pattern.compile(string + regex1);  
            Matcher m = p.matcher(exprStr); 
            if (m.find()) {
            	sb.append("int " + string + " = ((Number)map.get(\""+ string +"\")).intValue();");
			}else{
            	sb.append("double " + string + " = ((Number)map.get(\""+ string +"\")).doubleValue();");
			}            					
        } 
        
                
        sb.append("return ($w)("+ exprStr + ");");
        sb.append("}");
     
      
        CtMethod make = CtMethod.make(sb.toString(), mCtc);
		mCtc.addMethod(make);
        
             
       Class<?> proxyClass = mCtc.toClass();
       mCtc.detach();
       return (Expr) proxyClass.newInstance();
	}
	
	
	/**
	 * 
	 * 数学表达式
	 * @author nht
	 *
	 */
	public interface Expr{
		Object eval(Map<String, Number> map);		
	}
	
}
