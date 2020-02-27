package com.x.rpc.message;

import com.x.injection.ApplicationContext;
import com.x.net.cmd.Command;
import com.x.net.codec.IMessage;
import com.x.net.codec.Message;
import com.x.net.session.Session;
import com.x.rpc.client.RpcRequest;
import com.x.rpc.server.RpcResponse;
import com.x.rpc.server.RpcServiceContainer;
import com.x.util.ProtostuffCodec;

public class RpcReqMsg implements Command{
	private RpcServiceContainer container; 
	
	@Override
	public void execute(Session session, IMessage req) throws Exception {
			RpcRequest request = ProtostuffCodec.decode(req.getBody()); 			
			String className = request.getInterfaceName();
			String mName = request.getMethodName();
			if (container == null) {
				container = ApplicationContext.fetchBean(RpcServiceContainer.class);
			}
			Object obj = container.invoke(className, mName, request.getParams());
			RpcResponse response = new RpcResponse();
			response.setReqCode(request.getReqCode());
			if (obj != null) {
				response.setResult(obj);
			}
			session.sendMessage(Message.build(RpcMessageCode.SERVER_RES, response));
	}
}
