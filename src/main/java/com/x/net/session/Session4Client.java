package com.x.net.session;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.x.net.LifecycleListener;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

public class Session4Client extends Session4Server {
    
    private static final Logger logger = LoggerFactory.getLogger(Session4Client.class);
    
    private final Bootstrap bootstrap;
    private final String host;
    private final int port;
    private AtomicBoolean isConnecting;
    
    public Session4Client(LifecycleListener listener, Bootstrap bootstrap, String host, int port) {
        super(listener);
        this.bootstrap = bootstrap;
        this.host = host;
        this.port = port;
        this.isConnecting = new AtomicBoolean(false);
    }

    public Session4Client(Channel channel, LifecycleListener listener, Bootstrap bootstrap, String host, int port) {
        this(listener, bootstrap, host, port);
        this.bindChannel(channel);
    }

    @Override
    public boolean reconnect() {
        if(this.isActive()) return true;
        if(isConnecting.get()) return false;
        if(bootstrap.group().isShutdown()) return false;
        try {
            if(isConnecting.compareAndSet(false, true)) {
                if(!this.isActive()) {
                    ChannelFuture future = bootstrap.connect(host, port);
                    future.await(1, TimeUnit.SECONDS);
                    Channel channel = future.channel();
                    if(channel != null && channel.isActive()) {
                        this.bindChannel(channel);
                    } else {
                        logFailure(future.cause());
                    }
                }
            }
        } catch (Throwable e) {
            logFailure(e);
            //ignore
        } finally {
            isConnecting.set(false);
        }
        return this.isActive();
    }

    private void logFailure(Throwable e) {
        logger.warn("session connect[{}:{}] failed, cause {}", host, port, e.getMessage());
    }

}
