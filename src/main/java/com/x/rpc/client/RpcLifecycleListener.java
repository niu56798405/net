package com.x.rpc.client;

import com.x.net.LifecycleListener;
import com.x.net.cmd.Command;
import com.x.net.codec.IMessage;
import com.x.net.session.Session;
import com.x.tools.Log;

public class RpcLifecycleListener implements LifecycleListener{

	@Override
	public void onSessionRegister(Session session) {
//		Log.debug("rpc  connect success");	
	}

	@Override
	public void onMessageRecieve(Session session, IMessage message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessageSending(Session session, IMessage message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSessionUnRegister(Session session) {
//		reconnect(session);
	}
	
	
//	private void reconnect(Session session){
//		ActionExecutors.getScheduleExcutor().schedule(() -> {
//			boolean result = session.reconnect();
//			if (!result) {
//				Log.debug("rpcClient session reconnect fail, try again");
//				reconnect(session);
//			}else{
//				Log.debug("rpcClient session reconnect succ");
//			}
//		}, 60, TimeUnit.SECONDS);
//				
//	}
	

	@Override
	public void onCmdException(Session session, Command cmd, IMessage req, Throwable ex) {
		Log.error("rpc error", ex);
	}

}
