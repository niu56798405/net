package com.x.test;

import org.junit.Ignore;

import com.x.http.service.Http;
import com.x.http.service.Request;
import com.x.http.service.Response;
import com.x.http.service.ServiceContext;
import com.x.http.service.rest.HttpMethods;
import com.x.http.service.rest.RequestBody;
import com.x.http.service.rest.RequestHeader;
import com.x.http.service.rest.RequestParam;
import com.x.http.service.rest.RequestPath;
import com.x.http.service.rest.RestConfigurerAdapter;
import com.x.http.service.rest.RestService;
import com.x.http.service.rest.RestServiceBuilder;
import com.x.injection.ApplicationContext;
import com.x.injection.Injection;

import io.netty.handler.codec.http.HttpMethod;

@Ignore
@Http("something")
public class RestServiceTest implements RestService {
	
    @HttpMethods.GET
	public String dosomething0(@RequestParam int a, @RequestHeader long b, @RequestBody int x) {
    	long ret = (a + b) / x;
		return "dosomething0: (" + a + "+" + b + ")/"+x+"=" + ret;
	}
    
    @HttpMethods.GET("/{r}")
    public String dosomething1(@RequestPath int r, @RequestParam int a, @RequestHeader long b, @RequestBody int x) {
    	long ret = (a + b) * r / x;
    	return "dosomething1: (" + a + "+" + b + ")*" + r + "/" + x + "=" + ret;
    }
    
    @Http("/a/{v}")
    @HttpMethods.GET
    public String dosomething2(@RequestPath String v) {
        return v;
    }
	
	public static void main(String[] args) throws Exception {
		RestConfigurerAdapter configurer = new RestConfigurerAdapter(){
            @Override
            public void setBodyDecoder(BodyDecoderSetter setter) {
                setter.set((t, body) -> Integer.parseInt(new String(body)));
            }
            @Override
            public void setRespEncoder(RespEncoderSetter setter) {
                setter.set(resp -> new Response(resp.toString()));
            }
		};
		configurer.load();
		ApplicationContext.registBean(configurer);
		ApplicationContext.registBean(Injection.makeInstanceAndInject(RestServiceBuilder.class));
		ApplicationContext.registBean(Injection.makeInstanceAndInject(ServiceContext.class));
		ApplicationContext.fetchBean(ServiceContext.class).registService(RestServiceTest.class);
		
		printResp(doService(new Request(null, "something/2?a=2", null, HttpMethod.GET){
			@Override
			public String getHeader(String name) {
				return name.equals("b") ? "1" : "0";
			}
			@Override
			public byte[] content() {
				return "3".getBytes();
			}
		}));
		printResp(doService(new Request(null, "something?a=2", null, HttpMethod.GET){
			@Override
			public String getHeader(String name) {
				return name.equals("b") ? "1" : "0";
			}
			@Override
			public byte[] content() {
				return "3".getBytes();
			}
		}));
		printResp(doService(new Request(null, "something/a/0.1-2?a=2", null, HttpMethod.GET){
        }));
		
	}

	private static Response doService(Request request) {
		return ApplicationContext.fetchBean(ServiceContext.class).get(request.path()).invoke(request);
	}

	private static void printResp(Response resp) {
		byte[] bytes = new byte[resp.content.readableBytes()];
		resp.content.readBytes(bytes);
		System.out.println(new String(bytes));
	}
	
}
