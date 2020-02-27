package com.x.net.kcp;

import com.x.net.codec.IMessage;
import com.x.net.codec.MessageUtil;
import com.x.net.kcp.KcpOnUdp;
import com.x.net.session.Session;

import io.netty.channel.Channel;

public class KcpSession4Server extends Session {

    protected long id;
    protected Channel channel;
    private KcpOnUdp kcp;
    
    public KcpSession4Server(Channel channel,KcpOnUdp kcp) {      
        this.kcp = kcp;
        bindChannel(channel);
    }

    protected void bindChannel(Channel channel) {
        this.channel = channel;
        this.channel.attr(SESSION).set(this);
    }
    
    @Override
    public Channel channel() {
        return this.channel;
    }
    
    @Override
    public long id() {
        return this.id;
    }

    @Override
    public Session bind(long id) {
        this.id = id;
        return this;
    }

    @Override
    public void sendMessage(IMessage message) {
    	//128 长度满足大部分 消息长度需求
    	kcp.send(MessageUtil.encode(message, channel.alloc().buffer(128)));
    }
    
    @Override
    public void sendMessageAndClose(IMessage message) {        
    	sendMessage(message);
    	close();
    }
    
 
    
    @Override
    public boolean reconnect() {
        throw new IllegalArgumentException("Server session don`t support reconnect!");
    }

    @Override
    public void close() {
//    	kcp.release();
    	kcp.setClosed(true);
    }

	@Override
	public boolean isActive() {
		return !kcp.isClosed();
	}
}
