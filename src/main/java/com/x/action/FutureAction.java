package com.x.action;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * Future action
 * Method await for the action exec done
 * @author 
 *
 * @param <V>
 */
public abstract class FutureAction extends Action {
    
    private ReentrantLock lock;
    private volatile boolean isDone;
    private CountDownLatch latch;
    private volatile boolean isCancelled;
    private volatile boolean isRunning;
    private Throwable failure;

    public FutureAction(ActionQueue queue) {
        super(queue);
        this.lock = new ReentrantLock();
        this.latch = new CountDownLatch(1);
    }

    public ActionQueue getActionQueue() {
        return queue;
    }

    public void checkin() {
        queue.checkin(this);
    }
    
    public boolean cancel(boolean mayInterruptIfRunning) {
        try {
            lock.lock();
            if(isRunning) {
                return false;
            }
            if(isDone) {
                return false;
            }
            isCancelled = true;
            notifyDone();
            return true;
        } finally {
            lock.unlock();
        }
    }

    public boolean isCancelled() {
        try {
            lock.lock();
            return isCancelled;
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Set the result value and notify about operation completion.
     * @param result
     */
    public void done() {
        try {
            lock.lock();
            notifyDone();
        } finally {
            lock.unlock();
        }
    }
    
    public boolean isDone() {
        try {
            lock.lock();
            return isDone;
        } finally {
            lock.unlock();
        }
    }

    public void await() throws InterruptedException, ExecutionException {
        latch.await();
        try {
            lock.lock();
            if(isCancelled) {
                throw new CancellationException();
            } 
            if (failure != null) {
                throw new ExecutionException(failure);
            }
        } finally {
            lock.unlock();
        }
    }

    public void await(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean isTimeOut = !latch.await(timeout, unit);
        try {
            lock.lock();
            if (isTimeOut) {
                throw new TimeoutException();
            }
            if (isCancelled) {
                throw new CancellationException();
            } 
            if (failure != null) {
                throw new ExecutionException(failure);
            }
        } finally {
            lock.unlock();
        }
    }
    
    public void failure(Throwable failure) {
        try {
            lock.lock();
            this.failure = failure;
            notifyDone();
        }
        finally {
            lock.unlock();
        }
    }
    
    public boolean runable() {
        try {
            lock.lock();
            if(isCancelled) {
                return false;
            }
            
            isRunning = true;
            return true;
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Notify blocked listeners threads about operation completion.
     */
    protected void notifyDone() {
        isDone = true;
        latch.countDown();
    }
    
}