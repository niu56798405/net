package com.x.rpc.client;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.x.action.ActionExecutors;
import com.x.net.LifecycleListener;
import com.x.net.NetClient;
import com.x.net.cmd.CommandContext;
import com.x.net.codec.IMessage;
import com.x.net.session.Session;
import com.x.rpc.ServerType;
import com.x.rpc.message.RpcMessageCode;
import com.x.rpc.message.RpcResMsg;
import com.x.tools.Log;

public class RpcClient {
	private static int core = Runtime.getRuntime().availableProcessors();
	private static LifecycleListener listener = new RpcLifecycleListener();
	private static CommandContext cmdCtx = new CommandContext();
	static{
		cmdCtx.registCmd(RpcMessageCode.SERVER_RES, new RpcResMsg());
	}
	
	private NetClient client;
	private volatile Session[] sessions;
	private AtomicInteger idx = new AtomicInteger(0);	
	private int indexMax = core - 1;
	private String host;
	private int port;
	private int id;
	private ServerType type;
	
	public RpcClient(String host, int port, int id, ServerType type) {
		super();
		this.host = host;
		this.port = port;
		this.id = id;
		this.type = type;		
		client = new NetClient(listener, cmdCtx, (short)1000);		
		sessions = new Session[core];
		connect();	
	}


	
	private void connect() {		
		for(int i = 0; i < core; i++){
			try {
				sessions[i] = client.connect(host, port, 1);
			} catch (Exception e) {
				ActionExecutors.getScheduleExcutor().schedule(() -> connect(), 5, TimeUnit.SECONDS);
				e.printStackTrace();
				return;
			}			
		}
		Log.debug("rpc connc id:{} type:{}", id, type);
	}
	
	
	public void sendMessage(IMessage message){
		
		int index = idx.getAndIncrement() & (core -1);
//		if (index >= indexMax ) {
//			idx.set(0);
//			index = indexMax;
//
//		}
		Session session = sessions[index];
		if(session == null){
			try {
				Log.debug("no connection please wait reconc.....");
				Thread.sleep(1000 * 60);
				session = sessions[index];
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		session.sendMessage(message);
	}
	
	public LifecycleListener getListener() {
		return listener;
	}

	public CommandContext getCmdCtx() {
		return cmdCtx;
	}

	public NetClient getClient() {
		return client;
	}
	public void setClient(NetClient client) {
		this.client = client;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ServerType getType() {
		return type;
	}

	public void setType(ServerType type) {
		this.type = type;
	}

	public void close(){
		for (int i = 0; i < sessions.length; i++) {
			sessions[i].close();
		}
	}



	public String getHost() {
		return host;
	}



	public void setHost(String host) {
		this.host = host;
	}



	public int getPort() {
		return port;
	}


	public void setPort(int port) {
		this.port = port;
	}
	
}
