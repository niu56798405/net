package com.x.event;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Listener<T extends Event> {
    
	static final Logger logger = LoggerFactory.getLogger(Listener.class);
    
    final int group;
    final int type;
    
    public Listener(int group, int type) {
        this.group = group;
        this.type = type;
    }
    
    public Listener(int type) {
		this(0, type);
	}
    
    @SuppressWarnings("unchecked")
	final void onEvent0(Event event) {
    	try {
            onEvent((T) event);
        } catch (Throwable t) {
            logger.warn("Event notify throws ", t);
        }
    }
    
    public abstract void onEvent(T event) throws Exception;
    
    public static <X extends Event> Listener<X> of(int group, int type, Consumer<X> acceptor) {
    	return new SimpleListener<X>(group, type, acceptor);
    }
    
    static final class SimpleListener<X extends Event> extends Listener<X> {
    	final Consumer<X> acceptor;
		SimpleListener(int group, int type, Consumer<X> acceptor) {
			super(group, type);
			this.acceptor = acceptor;
		}
		@Override
		public void onEvent(X event) {
			acceptor.accept((X) event);
		}
	}

}
