package com.x.net;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.x.net.cmd.Command;
import com.x.net.cmd.CommandContext;
import com.x.net.codec.IMessage;
import com.x.net.session.Session;
import com.x.net.session.Session4Server;

/**
 * 
 * 消息分发器
 * @author 
 *
 */
@Sharable
public class NetMessageDispatcher extends ChannelInboundHandlerAdapter {
	
	private Logger logger = LoggerFactory.getLogger(NetMessageDispatcher.class);
	
	private CommandContext cmds;

	private LifecycleListener listener;
	private MessageInterceptor interceptor;
	
	public NetMessageDispatcher(LifecycleListener listener, CommandContext cmds) {
	    this(listener, cmds, null);
	}
	
	public NetMessageDispatcher(LifecycleListener listener, CommandContext cmds, MessageInterceptor interceptor) {
	    this.listener = listener;
	    this.cmds = cmds;
	    this.interceptor = interceptor;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		IMessage req = (IMessage) msg;
		Session session = Session.get(ctx);
		if(interceptor == null || (!interceptor.intercept(session, req))) {//被拦截的情况下不执行
		    exec(session, req);
		}
	}

    private void exec(Session session, IMessage req) {
        
        listener.onMessageRecieve(session, req);
        
        short code = req.getCode();
        Command cmd = cmds.get(code);
        if(cmd == null) {
            logger.warn("Can't find the command: " + code);
            return;
        }
        
        try {
	        cmd.execute(session, req);
	    } catch (Throwable ex) {
	        listener.onCmdException(session, cmd, req, ex);
	    }
    }

	@Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
	    Session session = new Session4Server(ctx.channel(), listener);
	    listener.onSessionRegister(session);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        listener.onSessionUnRegister(Session.get(ctx));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

}
