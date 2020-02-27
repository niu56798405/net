package com.x.test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.x.tools.ExprUtil;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

public class ExprTest {
	public static void main(String[] args) throws Exception {
		int num = 1;
		System.out.println((++num) + (++num));
		
		String exprStr = "((level | 1) + 100 + vipLevel + (exp >> 10)) % 10 > 10";
//		Expr expr = buildExpr(exprStr);
		com.x.tools.ExprUtil.Expr  expr = ExprUtil.buildExpr(exprStr);
		com.x.tools.ExprUtil.Expr  expr1 = ExprUtil.buildExpr(exprStr);

		Map<String, Number> map = new HashMap<>();
		map.put("level",  56);
		map.put("vipLevel",  10);
		map.put("exp",  10);
		long start = System.currentTimeMillis();

		int time = 1000;
		for (int i = 0; i < time; i++) {
			Object eval = expr.eval(map);
//			System.out.println("value :" + eval );
		}
		long end = System.currentTimeMillis();
		System.out.println(end - start);
		
		String js = "level >> 1 ";

		javax.script.ScriptEngineManager scriptManager = new javax.script.ScriptEngineManager();
		javax.script.ScriptEngine jsEngine = scriptManager.getEngineByName("JavaScript");
		jsEngine.put("level", 56.8);
		jsEngine.put("vipLevel", 10);
		jsEngine.put("exp",  10);
		start = System.currentTimeMillis();

		for (int i = 0; i < time; i++) {
			Object eval2 = jsEngine.eval(exprStr);			
//			System.out.println(eval2);
		}	
		end = System.currentTimeMillis();
		System.out.println(end - start);
		

		
	}
	
	
	
	public static Expr buildExpr(String exprStr) throws Exception{
        String regex = "[a-zA-Z_]+[a-zA-Z0-9_]*";		
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
        		continue;
        	}  	
			splitArray.add(group);  
        } 
		if (exprStr.contains("exp")) {
			splitArray.add("exp");
		}
        
        
		String interfaceName = Expr.class.getCanonicalName();
 	   	ClassPool mPool = ClassPool.getDefault();
        CtClass mCtc = mPool.makeClass(interfaceName + "_proxy_" + "Expr");      
        mCtc.addInterface(mPool.getCtClass(Expr.class.getName())); 
         
        List<String> methodList = new ArrayList<String>();
        
        StringBuilder sb = new StringBuilder();
        sb.append("public Object eval(java.util.Map map) {");
        
        for (int i = 0; i < splitArray.size(); i++) {       	        	
        	String string = splitArray.get(i);
            String regex1 = "[\\s]*[<>]{2}";		
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
     
        methodList.add(sb.toString());
        for (String methodStr : methodList) {   
            CtMethod make = CtMethod.make(methodStr, mCtc);
			mCtc.addMethod(make);
        }
             
       Class<?> proxyClass = mCtc.toClass();
       mCtc.detach();
       return (Expr) proxyClass.newInstance();
	}
	
	
	public interface Expr{
		Object eval(Map<String, Number> map);
		
	}
	
}
