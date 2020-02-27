package com.x.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.junit.Ignore;

import com.x.http.HttpServer;
import com.x.http.service.Request;
import com.x.http.service.Response;
import com.x.http.service.Response.ContentType;
import com.x.http.service.Service;
import com.x.http.service.ServiceContext;

@Ignore
public class HttpServerTest {

    public static void main(String[] args) {
        ServiceContext ctx = new ServiceContext();
   	 	File file = new File("aaa.txt");  
   	 	StringBuilder content = new StringBuilder();
        BufferedReader reader = null; 
//        System.out.println(file.getAbsolutePath());  

        BufferedWriter out = null;
	     try {  
	         System.out.println("以行为单位读取文件内容，一次读一整行：");  
	         reader = new BufferedReader(new FileReader(file));  
	         String tempString = null;  
	         int line = 1;  
	         // 一次读入一行，直到读入null为文件结束  
	         
	           
	         out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("abc.txt", true)));   
	         
	         while ((tempString = reader.readLine()) != null) {  
	             // 显示行号  
//	             System.out.println("line " + line + ": " + tempString);
	             if (tempString.endsWith("anim")) {
		             out.write(tempString + "\r\n");   		
				}
	             content.append(tempString);
	             line++;  
	         }  
	         reader.close();  
	     } catch (IOException e) {  
	         e.printStackTrace();  
	     } finally {  
	         if (reader != null) {  
	             try {  
	                 reader.close();  
	                 out.close();
	             } catch (Exception e1) {  
	             }  
	         }  
	     }  
//
        ctx.registService("test", new Service() {
            @Override
            public Response service(Request req) {              	
                return new Response(ContentType.JSON, req.getParam("time"));
            }
        });
        HttpServer server = new HttpServer();
        server.start(8090, ctx);
    }
    
    public static void method1(String file, String conent) {   
        BufferedWriter out = null;   
        try {   
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));   
            out.write(conent + "\r\n");   
        } catch (Exception e) {   
            e.printStackTrace();   
        } finally {   
            try {   
            	if(out != null){
            		out.close();   
                }
            } catch (IOException e) {   
                e.printStackTrace();   
            }   
        }   

    }
    
}
