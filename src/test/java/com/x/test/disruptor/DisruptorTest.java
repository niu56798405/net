package com.x.test.disruptor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import javax.swing.table.TableColumn;
import javax.swing.text.html.HTMLDocument.HTMLReader.TagAction;

import com.gaea.concurrent.FastBlockingQueue;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.x.action.Action;
import com.x.action.ActionExecutor;
import com.x.action.ActionExecutors;
import com.x.action.ActionQueue;

public class DisruptorTest {
	public static int size = 1000000;
	
	public static void main(String[] args) {
		
		ExecutorService single = Executors.newSingleThreadExecutor();
		EventFactory<LongEvent> eventFactory = (EventFactory<LongEvent>) new LongEventFactory();
		ExecutorService executor = Executors.newCachedThreadPool();
		int ringBufferSize = 1024 * 1024; // RingBuffer 大小，必须是 2 的 N 次方；
		        
		Disruptor<LongEvent> disruptor = new Disruptor<LongEvent>(eventFactory,
		                ringBufferSize, executor, ProducerType.MULTI, 
		                new YieldingWaitStrategy());
		        
		EventHandler<LongEvent> eventHandler = (EventHandler<LongEvent>) new LongEventHandler();
		disruptor.handleEventsWith(eventHandler);        
		disruptor.start();
		
	    ActionExecutor actionExecutor = ActionExecutors.newCached("Test", 1, 5);
        ActionQueue queue = new ActionQueue(actionExecutor);
		List<TAction> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
        	list.add(new TAction(queue));
		}
        
        
       BlockingQueue<Runnable> fastBlockingQueue = new FastBlockingQueue<Runnable>();
       ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, fastBlockingQueue);
        
        
       final ConcurrentLinkedQueue<Runnable> runnables = new ConcurrentLinkedQueue<>();
       
       
       Thread thread = new Thread(()->{
    	   while (true) {
    			Runnable poll = runnables.poll();
				if (poll == null) {
					LockSupport.parkNanos(1);
				}else{
					poll.run();
				}   					 			
		}    	   
       });
       thread.start();
       
		long s1 = System.currentTimeMillis();

//       for (int i = 0; i < size; i++) {
//    	   final int num = i;
//    	   runnables.add(()->{
//    		   if (num == DisruptorTest.size - 1) {
//					System.out.println("concurrnt queue:>>>>>>>>>>  time : " + (System.currentTimeMillis() - s1));
//				}
//    	   });
//       }
       
		
		
		executor.execute(()->{
			for (int i = 0; i < size; i++) {
			    
				try {
			
				    final int num = i; 
				    threadPoolExecutor.execute(()->{
						if (num == DisruptorTest.size - 1) {
							System.out.println("queue:>>>>>>>>>>  time : " + (System.currentTimeMillis() - s1));
						}
							
				    });
				    				 
				} finally{
				}
			}
		});
//		
//		// 发布事件；
//		RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();
//		executor.execute(()->{
//			for (int i = 0; i < size; i++) {
//				long sequence = ringBuffer.next();//请求下一个事件序号；
//			    
//				try {
//				    LongEvent event = ringBuffer.get(sequence);//获取该序号对应的事件对象；
//				    event.setValue(i);
//				    event.time = s1;
//				    				 
//				} finally{
//				    ringBuffer.publish(sequence);//发布事件；
//				}
//			}
//		});

		

		long s2 = System.currentTimeMillis();
		System.out.println("time use:"+(s2 - s1));
		
		
	}
	
 	
}
