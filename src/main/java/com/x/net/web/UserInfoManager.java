package com.x.net.web;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jboss.resteasy.plugins.server.netty.NettyUtil;

import com.x.net.session.Session;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;

public class UserInfoManager {
	private static ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);
    protected static final AttributeKey<String> SESSION = AttributeKey.valueOf("id");

    private static ConcurrentMap<String, Channel> userInfos = new ConcurrentHashMap<>();
    /**
     * 登录注册 channel
     *
     *  
     */
    public static void addChannel(Channel channel, String uid) {
    	channel.attr(SESSION).set(uid);
    	userInfos.put(uid, channel);
    }

    public static void removeChannel(Channel channel){
    	String string = channel.attr(SESSION).get();
    	userInfos.remove(string);
    	System.out.println("user :" + string + " leave >>>>");
    }
    
    /**
     * 普通消息
     *
     * @param message
     */
    public static void broadcastMess(String uid,String message,String sender) {
            try {
                rwLock.readLock().lock();
                Channel ch = userInfos.get(uid);
                String backmessage=sender+","+message;
                ch.writeAndFlush(new TextWebSocketFrame(backmessage));
                                 
            } finally {
                rwLock.readLock().unlock();
            }
        
    }
 
    
    
    
}
