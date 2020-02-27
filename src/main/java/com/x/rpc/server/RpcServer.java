package com.x.rpc.server;

import com.x.net.LifecycleListener;
import com.x.net.NetServer;
import com.x.net.cmd.CommandContext;
import com.x.rpc.message.RpcMessageCode;
import com.x.rpc.message.RpcReqMsg;
public class RpcServer  {
	NetServer netServer;
	
    public void start(int port) {		
		netServer = new NetServer();                
        LifecycleListener lifecycleListener = new RpcServerLifecycleListener();
		CommandContext commondCtx = new CommandContext();		
		commondCtx.registCmd(RpcMessageCode.CLIENT_REQ, new RpcReqMsg());
		commondCtx.registCmd((short)1000, (s, m)->{});

		netServer.start(port, lifecycleListener, commondCtx);	
	}
	
	
}
