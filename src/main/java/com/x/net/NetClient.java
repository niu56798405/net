package com.x.net;

import com.x.action.ThreadsFactory;
import com.x.net.NetChannelInitializer.ClientInitializer;
import com.x.net.cmd.CommandContext;
import com.x.net.codec.CodecFactory;
import com.x.net.codec.IMessage;
import com.x.net.codec.Message;
import com.x.net.codec.MessageBuilder;
import com.x.net.session.Session;
import com.x.net.session.Session4Client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NetClient {
    
    private static EventLoopGroup group  = new NioEventLoopGroup(8, new ThreadsFactory("netty.client"));;
    
    private Bootstrap bootstrap;
    
    private LifecycleListener listener;
    
    
    
    public NetClient(LifecycleListener listener, CommandContext cmdCtx, short hearbeatProtocol) {
        this(listener, cmdCtx, null, hearbeatProtocol);
    }
    public NetClient(int threads, LifecycleListener listener, CommandContext cmdCtx, short hearbeatProtocol) {
        this(listener, cmdCtx, null, hearbeatProtocol);
    }
    
    public NetClient(LifecycleListener listener, CommandContext cmdCtx, MessageInterceptor interceptor, short hearbeatProtocol) {
        this(Message.BUIDLER, listener, cmdCtx, interceptor, Message.BUIDLER.build(hearbeatProtocol));
    }
    public NetClient(int threads, LifecycleListener listener, CommandContext cmdCtx, MessageInterceptor interceptor, short hearbeatProtocol) {
        this(threads, Message.BUIDLER, listener, cmdCtx, interceptor, Message.BUIDLER.build(hearbeatProtocol));
    }
    
    public NetClient(MessageBuilder builder, LifecycleListener listener, CommandContext cmdCtx, MessageInterceptor interceptor, IMessage hearbeat) {
        this(Runtime.getRuntime().availableProcessors(), builder, listener, cmdCtx, interceptor, hearbeat);
    }
    public NetClient(int threads, MessageBuilder builder, LifecycleListener listener, CommandContext cmdCtx, MessageInterceptor interceptor, IMessage hearbeat) {
        this.listener = listener;
        this.bootstrap = new Bootstrap();
//        this.group = new NioEventLoopGroup(threads, new ThreadsFactory("netty.client"));
        NetMessageDispatcher dispatcher = new NetMessageDispatcher(listener, cmdCtx, interceptor);
        
        this.bootstrap.group(group)
             .channel(NioSocketChannel.class)
             .option(ChannelOption.TCP_NODELAY, true)
             .handler(new ClientInitializer(dispatcher, new CodecFactory(builder, false), hearbeat));
    }
    
    public Session buildSession(String host, int port, long sessionId) {
        return new Session4Client(listener, bootstrap, host, port).bind(sessionId);
    }
    
    public Session connect(String host, int port, long sessionId) {
        try {
            Session session = new Session4Client(bootstrap.connect(host, port).channel(), listener, bootstrap, host, port);
            session.bind(sessionId);//? sessionId
            return session;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    public void stop() {
        group.shutdownGracefully();
    }
    
    public void close(Channel channel) {
        channel.close().awaitUninterruptibly();
    }

    public boolean isShutdown() {
        return group.isShuttingDown();
    }
    
       
}
