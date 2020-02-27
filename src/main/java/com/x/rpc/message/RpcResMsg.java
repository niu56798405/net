package com.x.rpc.message;

import com.x.injection.ApplicationContext;
import com.x.net.cmd.Command;
import com.x.net.codec.IMessage;
import com.x.net.session.Session;
import com.x.rpc.client.RpcInvoker;
import com.x.rpc.server.RpcResponse;
import com.x.util.ProtostuffCodec;

public class RpcResMsg implements Command{

	@Override		
	public void execute(Session session, IMessage req) throws Exception {
			RpcResponse parseFrom = ProtostuffCodec.decode(req.getBody());		
			long code = parseFrom.getReqCode();
			RpcInvoker invoker = ApplicationContext.fetchBean(RpcInvoker.class);
			invoker.callBack(code, parseFrom.getResult());			

	}	
}
