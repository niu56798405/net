package com.x.rpc.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.x.injection.Bean;
import com.x.net.codec.Message;
import com.x.rpc.message.RpcMessageCode;
import com.x.tools.Log;
@Bean
public class RpcInvoker {
	private static long OUT_TIME = 60;
	
	private  AtomicLong code = new AtomicLong();
	private Map<Long, Task<?>> tasks = new ConcurrentHashMap<>(); 
	
	public <T>T invoke(RpcRequest req, Class<T> returnType){
	
		long taskCode = code.getAndIncrement();
		req.setReqCode(taskCode);		
		if (returnType != void.class) {
			Task<T> task = new Task<>(req, taskCode);			
			tasks.put(taskCode, task);			
			sendRpcReq(Message.build(RpcMessageCode.CLIENT_REQ, req));		
			try {
				return task.get();
			} catch (Exception e) {
				Log.error(req.toString(), e);
			} 
		}
		sendRpcReq(Message.build(RpcMessageCode.CLIENT_REQ, req));
		return null;
	}
	
	private void sendRpcReq(Message message) {
		RpcClient client = RpcClientManager.get();
		client.sendMessage(message);
	}
	
	
	public void callBack(long code, Object result){
		Task<?> task = tasks.remove(code);
		if (task != null) {
			task.call(result);
		}
	}

	public class Task<V> {
		private V value;
		private CountDownLatch countDownLatch;
		private long code;		
		public Task(RpcRequest req, long code) {					
			this.countDownLatch = new CountDownLatch(1);
			this.code = code;
		}

		@SuppressWarnings("unchecked")
		public void call(Object v){
			this.value = (V)v;
			countDownLatch.countDown();
		}
		
		public V get() throws InterruptedException{
			boolean await = countDownLatch.await(OUT_TIME, TimeUnit.SECONDS);
			if (!await) {
				tasks.remove(code);				
				throw new InterruptedException("rpc req time out");
			}
			return value;
		}
		
	}
		
}
