package com.x.test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.x.rpc.RpcService;
import com.x.rpc.server.MethodProxyBuilder;
import com.x.rpc.server.RpcMethod;

@RpcService
public class GreetingServiceImpl implements GreetingService {
	public int a = 6;
	
    public float addFloat(float a, Float b){
    	return a+b;
    }

	
    public int getA() {
		return a;
	}

	public void setA(int a) {
		this.a = a;
	}

	public String sayHello(String name) {
        return "Hello " + name;
    }
    
    public void print(){
//    	Log.debug("print >>>>>");
    }

	@Override
	public int addInt(int a, Integer b) {
		return a + b;
	}

	@Override
	public double addDouble(Double a, Double b) {
		return a + b;
	}

	@Override
	public float addFloat(Float a, Float b) {	
		return a + b;
	}

	@Override
	public long addLong(Long a) {
		return a;
	}

	@Override
	public ConsumerApplication getConsumer(ConsumerApplication application) {
		application.setA(application.getA() + 999);
		application.getList().add(988);
		application.setBool(true);
		return application;
	}

	@Override
	public List<ConsumerApplication> getConsumers(List<ConsumerApplication> applications) {
		applications.add(new ConsumerApplication());
		return applications;
	}

	@Override
	public void mapTest(Map<Integer, ConsumerApplication> map) {
    	System.out.println(map.size());
	}

	@Override
	public Set<ConsumerApplication> setTest(Set<ConsumerApplication> applications) {
		return applications;
	}
    
	
	public static void main(String[] args) throws Exception {
		Method method = GreetingServiceImpl.class.getDeclaredMethod("print", null);
		
		GreetingServiceImpl greetingServiceImpl = new GreetingServiceImpl();
		int time = 100000000;
		long start = System.currentTimeMillis();
		for(int i = 0; i<time; i++){
			method.invoke(greetingServiceImpl, null);
		}
		long end = System.currentTimeMillis();
		System.out.println(end - start);		
		 start = System.currentTimeMillis();
		for(int i = 0; i<time; i++){
			greetingServiceImpl.print();
		}
		 end = System.currentTimeMillis();
		System.out.println(end - start);
		
		for(Method m : GreetingServiceImpl.class.getDeclaredMethods()){
			boolean check = RpcMethod.check(m);
			if (check) {
				RpcMethod build = MethodProxyBuilder.build(greetingServiceImpl, m);
			}
		}
		
		 start = System.currentTimeMillis();
//		for(int i = 0; i<time; i++){
//			build.invoke(null);
//		}
		 end = System.currentTimeMillis();
		System.out.println(end - start);
		
	}


	
	
	
}