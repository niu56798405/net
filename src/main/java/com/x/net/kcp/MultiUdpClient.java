package com.x.net.kcp;


import java.util.HashMap;
import java.util.Map;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

public abstract class MultiUdpClient extends ChannelInitializer<Channel> implements  KcpListerner {

    private int clientNum;
	private EventLoopGroup bossGroup;

    protected Map<Integer, Channel> channels = new HashMap<Integer, Channel>();
    
    protected Map<Integer, Bootstrap> bootStraps = new HashMap<Integer, Bootstrap>();
    public MultiUdpClient() {
        this(new NioEventLoopGroup(), 1);
    }
    
    public MultiUdpClient(int clientNum) {
        this(new NioEventLoopGroup(10), clientNum);
    }
    
    
    public MultiUdpClient(EventLoopGroup bossGroup,int clientNum) {
        this.bossGroup = bossGroup;
        this.clientNum = clientNum;
        for(int i=0;i<clientNum;i++){
        	  	
			Bootstrap bootstrap = new Bootstrap()
                      .group(bossGroup)
                      .channel(NioDatagramChannel.class)
                      .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                      .option(ChannelOption.SO_BROADCAST, true)
                      .handler(this);
        	  bootStraps.put(i, bootstrap);           	  
        }
      
    }


    public void connect() throws Exception {
    	for(int i=0;i<clientNum;i++){    			
    	 	ChannelFuture channelFuture = bootStraps.get(i).bind(0);
            channels.put(i, channelFuture.channel());
			channelFuture.sync();
    	 	Thread.sleep(20);    	 	

    	}
		Thread.sleep(5000);

               
    }

//    public ChannelFuture stop() throws Exception {
//        return channel.close();
//    }



    public EventLoopGroup getBossGroup() {
        return bossGroup;
    }



}
