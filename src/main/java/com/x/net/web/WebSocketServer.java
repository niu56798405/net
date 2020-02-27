package com.x.net.web;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.x.action.ThreadsFactory;
import com.x.net.LifecycleListener;
import com.x.net.MessageInterceptor;
import com.x.net.NetMessageDispatcher;
import com.x.net.cmd.CommandContext;
import com.x.net.codec.Message;
import com.x.net.codec.MessageBuilder;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * 网络服务启动
 * @author 
 */
public class WebSocketServer {
	
	private static Logger logger = LoggerFactory.getLogger(WebSocketServer.class);
	
	private Channel bossChannel;
	private NioEventLoopGroup bossGroup;
	private NioEventLoopGroup workerGroup;
	
	
	public void start(int port) {		
		start(port, null, null);
	}	
	public void start(int port, LifecycleListener listener, CommandContext cmdCtx) {
	    this.start(port, listener, cmdCtx, null);
	}
	public void start(int port, int threads, LifecycleListener listener, CommandContext cmdCtx) {
	    this.start(port, threads, listener, cmdCtx, null);
	}
	
	public void start(int port, LifecycleListener listener, CommandContext cmdCtx, MessageInterceptor interceptor) {
	    this.start(port, Runtime.getRuntime().availableProcessors() * 2, listener, cmdCtx, interceptor);
	}
	public void start(int port, int threads, LifecycleListener listener, CommandContext cmdCtx, MessageInterceptor interceptor) {
	    this.start(port, threads, Message.BUIDLER, listener, cmdCtx, interceptor);
	}
	
	public void start(int port, int threads, MessageBuilder builder, LifecycleListener listener, CommandContext cmdCtx, MessageInterceptor interceptor) {
	    bossGroup = new NioEventLoopGroup(1, new ThreadsFactory("netty.boss"));
	    workerGroup = new NioEventLoopGroup(threads, new ThreadsFactory("netty.worker"));
//        NetMessageDispatcher dispatcher = new NetMessageDispatcher(listener, cmdCtx, interceptor);
        
        ServerBootstrap bootstrap =
	            new ServerBootstrap()
    	            .group(bossGroup, workerGroup)
    	            .channel(NioServerSocketChannel.class)
    	            .childHandler(new ChannelInitializer<SocketChannel>(){
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("http-codec", new HttpServerCodec()); 
                            pipeline.addLast("aggregator", new HttpObjectAggregator(65536)); // Http消息组装  
                            pipeline.addLast("http-chunked", new ChunkedWriteHandler()); // WebSocket通信支持  
                            pipeline.addLast(new SocketHandler());//自定义处理类

                        }
                 })
    	          .option(ChannelOption.SO_BACKLOG, 128)  
    	          .childOption(ChannelOption.SO_KEEPALIVE, true);
    	        
        
        workerGroup.setIoRatio(100);//优先处理网络任务(IOTask)再处理UserTask
        
	    try {
	        bossChannel = bootstrap.bind(port).sync().channel();
            logger.info("NetServer listening to port : " + port);
        } catch (InterruptedException e) {
            logger.error("NetServer start failed ...", e);
        }
	}
	
	public void stop() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
	    bossChannel.close().awaitUninterruptibly();
	}
	
	public static void main(String[] args) {
		WebSocketServer  server =  new WebSocketServer();
		server.start(9999);
	}
	
}
