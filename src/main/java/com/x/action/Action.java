package com.x.action;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.x.tools.Monitor;
import com.x.tools.Monitor.Sequential;

public abstract class Action implements Runnable, Sequential {
    
    protected static final Logger logger = LoggerFactory.getLogger(Action.class);

    protected ActionQueue queue;
    protected long createTime;

    public Action(ActionQueue queue) {
        this.queue = queue;
        this.createTime = System.currentTimeMillis();
    }
    
    public Action(){
        this.createTime = System.currentTimeMillis();
    }
    
    public ActionQueue getActionQueue() {
        return queue;
    }
    
    public void checkin() {
        this.queue.checkin(this);
    }
    
    @Override
    public final void run() {
        try {
            if(runable()) {
                long createTime = this.createTime;
                long start = System.currentTimeMillis();
                exec();
                long end = System.currentTimeMillis();
                Monitor.log(getClazz(), createTime, start, end, this);
            }
        } catch (Throwable e) {
            logger.error("Execute exception: " + getClazz().getName(), e);
        } finally {
            queue.checkout(this);
            done();
        }
    }
    
    protected boolean runable() {
        return true;
    }

    protected void done() {
        //do nothing
    }

    protected abstract void exec();
    
    protected Class<?> getClazz() {
    	return this.getClass();
    }
    
	public final int waitings() {
		return queue.size();
	}

	@Override
    public String toString() {
        return getClazz().getName() + " [" + DateTimeFormatter.ofPattern("MM-dd HH:mm:ss").format(ZonedDateTime.now()) + "]";
    }

}
