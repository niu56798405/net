package com.x.test;

import org.junit.Ignore;

import com.x.game.protocol.simple.SimpleProto.StringMsg;
import com.x.net.LifecycleListener;
import com.x.net.NetClient;
import com.x.net.cmd.Command;
import com.x.net.cmd.CommandContext;
import com.x.net.codec.IMessage;
import com.x.net.codec.Message;
import com.x.net.session.Session;
import com.x.tools.Log;
@Ignore
public class ClientTest {
	public static void main(String[] args) {
		LifecycleListener li = new LifecycleListener() {
			
			@Override
			public void onSessionUnRegister(Session session) {
				Log.debug("client session close");
			}
			
			@Override
			public void onSessionRegister(Session session) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onMessageSending(Session session, IMessage message) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onMessageRecieve(Session session, IMessage message) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onCmdException(Session session, Command cmd, IMessage req, Throwable ex) {
				// TODO Auto-generated method stub
				
			}
		};
		CommandContext cmdCtx = new CommandContext();
		cmdCtx.registCmd((short) 2000, new Command() {
			
			@Override
			public void execute(Session session, IMessage req) throws Exception {
				System.out.println("server ");
				
			}
		});
		NetClient client = new NetClient(li, cmdCtx, (short)1000);
		Session session = client.connect("localhost", 8000, 99992);
		short code = 1001;
	
		Message build = Message.build(code, 8888);
		session.sendMessage(build);
		code = 1003;
		StringMsg.Builder builder = StringMsg.newBuilder();
		builder.setValue("精神病院vip");
		Message msg = Message.build(code, builder.build());
		msg.setId(8888);		
		session.sendMessage(msg);
	}
}
