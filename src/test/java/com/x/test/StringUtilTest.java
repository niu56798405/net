package com.x.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtilTest {
	
	     
	public static Matcher getMatcher(String regex, String source) { 		        		  		         
		Pattern pattern = Pattern.compile(regex);  		        
		Matcher matcher = pattern.matcher(source);  	        	
		return matcher;  		     
	} 
	
	public static void main(String[] args) {
		String source = "vip*2 + lv * 10";
        String regex = "[a-zA-Z]+";		
        Pattern  pattern=Pattern.compile(regex);  
        Matcher  ma=pattern.matcher(regex);  
   
        while(ma.find()){  
            System.out.println(ma.group());  
        }  
		
		
	}
	
	
}
