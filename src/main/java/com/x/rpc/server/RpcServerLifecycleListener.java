package com.x.rpc.server;

import com.x.net.LifecycleListener;
import com.x.net.cmd.Command;
import com.x.net.codec.IMessage;
import com.x.net.session.Session;
import com.x.tools.Log;

public class RpcServerLifecycleListener implements LifecycleListener{

	@Override
	public void onSessionRegister(Session session) {
		Log.debug("client rpc  connect success");
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCmdException(Session session, Command cmd, IMessage req, Throwable ex) {
		Log.error("rpc error", ex);
	}

}
