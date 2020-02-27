package com.x.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.x.action.ActionExecutors;
import com.x.game.GameInitializer;
import com.x.game.player.Player;
import com.x.http.HttpServer;
import com.x.injection.ApplicationContext;
import com.x.net.NetServer;

import com.x.rpc.ServerType;
import com.x.rpc.client.RpcClient;
import com.x.rpc.client.RpcProxy;
import com.x.rpc.client.RpcClientManager;
import com.x.rpc.server.RpcServer;
import com.x.test.ConsumerApplication.Type;
import com.x.tools.Log;


public class RpcTest {


    public static void main(String[] args) throws Exception {
        try {
        		
    			
    		ApplicationContext.initialize("com.x.*", "");
    		RpcServer  server = new RpcServer();
    		server.start(8088);
    		
    		RpcClient rpcClient = new RpcClient("127.0.0.1", 8088, 1, ServerType.GAME);
    		
            
            
            RpcClientManager.set(rpcClient);
            RpcProxy proxy = ApplicationContext.fetchBean(RpcProxy.class);
            GreetingService service = proxy.getProxy(GreetingService.class);
            int addInt99 = service.addInt(1, 8);
            System.out.println(addInt99);
            service.print();
            List<ConsumerApplication> applications = new ArrayList<>();
            List<Integer> list = new LinkedList<>();
            list.add(5656565);
            list.add(5987456);
            Executor executor = Executors.newCachedThreadPool();
			long now0 = System.currentTimeMillis(); 
            for(int j = 0; j< 2; j++){
            	final int k = j;
                executor.execute(()->{
                    for(int i = 0; i< 10000; i++){
        				long now = System.currentTimeMillis();
        				service.print();
//        				long addLong = service.addLong(now);
//        				System.currentTimeMillis() - addLong);
//        	            ConsumerApplication consumerApplication = new ConsumerApplication();
//        	            consumerApplication.setList(list);
//                        applications.add(consumerApplication);
                    }
    				System.out.println(k + " rpc time :" + (System.currentTimeMillis() - now0));

                });
            }		


			
			
       	 	long start0 = System.currentTimeMillis();

            long end0 = System.currentTimeMillis();
      		System.out.println(end0 - start0);

            
            Map map = new HashMap<>();
            for(int i = 0; i< 10; i++){
	            ConsumerApplication consumerApplication = new ConsumerApplication();
            	map.put(i, consumerApplication);
            }
            long start1 = System.currentTimeMillis();
            System.out.println(start1);
            service.mapTest(map);
            long end1 = System.currentTimeMillis();
			System.out.println(end1 - start1);
		
			Set<ConsumerApplication> setTest = service.setTest(new HashSet<>(map.values()));	

     
         
            
            String sayHello = service.sayHello("haohan");
            long s1 = System.currentTimeMillis();
            for(int k = 0; k< 100; k++){
                service.print();
            }
            
            System.out.println(sayHello +" : "+ (System.currentTimeMillis() - s1));
            
            int addInt = service.addInt(1, 2);
            System.out.println("add int :" + addInt);
            
            Double addD = service.addDouble(3.0, 4.0);
            System.out.println("add d :" + addD);
            
            float addF = service.addFloat(1f, new Float(1));
            addF = service.addFloat(new Float(2), new Float(1));
            System.out.println("add f :" + addF);
            
            long addLong = service.addLong(998L);
            System.out.println("add l :" + addLong);
            ConsumerApplication application = new ConsumerApplication();
            application.setA(5);
            application.setBool(false);
            application.setType(Type.B);
            application.setList(list);
            int[] a = new int[2];
            a[0] = 1;
            a[1] = 998;
            application.setIntArray(a);
            ConsumerApplication consumer = service.getConsumer(application);
            System.out.println(consumer);
            
           	long start = System.currentTimeMillis();

            List<ConsumerApplication> consumers = service.getConsumers(applications);
            ConsumerApplication consumerApplication = consumers.get(0);
			System.out.println(consumerApplication);
            	
            long end = System.currentTimeMillis();
     		System.out.println(" time use ："+ (end - start)  + " size: " + consumers.size());
//
//            for(int i = 0; i< 4; i++){
//            	ActionExecutors.getExecutors().get(0).execute(()->{
//            
//            	 	long startN = System.currentTimeMillis();
//            	 	
//                    List<ConsumerApplication> consumersN = service.getConsumers(applications);
//                    for(int k = 0; k< 10000; k++){
//                        consumersN = service.getConsumers(applications);
//                    }
//
////                    ConsumerApplication consumerApplicationN = consumersN.get(0);
////        			System.out.println(consumerApplicationN);
//                    	
//                    long endN = System.currentTimeMillis();
//             		System.out.println("time use :"+ (endN - startN));
//            		            		
//            	});          	
//           
//            }
//            
  
     		
     	 	 start = System.currentTimeMillis();
     	 	 
            consumers = service.getConsumers(applications);
            consumerApplication = consumers.get(0);
			System.out.println(consumerApplication);
            	
            end = System.currentTimeMillis();
     		System.out.println(" time use1 :" + (end - start));
            
            
//            ApplicationContext.fetchBean(NodeConnector.class).connect();
          
            
        } catch (Exception ex) {
            Log.error("启动错误!", ex);
            System.exit(-1);
        }
    }
}



