package com.x.net.session;

import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

public class SessionSet implements Iterable<Session> {
    
    private Session[] sessions;
    private volatile int capacity;
    private volatile int idx;
    private volatile int size;
    private ReentrantLock lock;
    
    public SessionSet(int capacity) {
        this.capacity = capacity;
        this.sessions = new Session[capacity];
        this.idx = -1;
        this.size = 0;
        this.lock = new ReentrantLock();
    }

    public void add(Session session) {
        lock.lock();
        try {
            for (int i = 0; i < this.capacity; i++) {
                if(sessions[i] == null) {
                    sessions[i] = session;
                    ++ size;
                    break;
                }
            }
        } finally {
            lock.unlock();
        }
    }
    
    public Session get() {
        if(this.size == 0) return null;
        
        lock.lock();
        try {
            if(this.size == 0) return null;//double check
            
            int next = this.idx;
            for(int i=0; i<capacity; i++) {//只找一圈
                ++ next;
                if(next >= capacity) next = 0;
                
                Session ret = sessions[next];
                if(ret != null) {
                    this.idx = next;
                    return ret;
                }
            }
            return null;
        } finally {
            lock.unlock();
        }
    }
    
    public Session remove(Session session) {
        for (int i = 0; i < capacity; i++) {
            if(sessions[i] == session) {
                return remove(i);
            }
        }
        return null;
    }
    
    private Session remove(int idx) {
        lock.lock();
        try {
            return remove0(idx);
        } finally {
            lock.unlock();
        }
    }

    private Session remove0(int idx) {
        Session re = sessions[idx];
        sessions[idx] = null;
        if(re != null) {
            --this.size;
            re.close();
        }
        return re;
    }
    
    public void close() {
        lock.lock();
        try {
            for (int i = 0; i < capacity; i++) {
                remove0(i);
            }
        } finally {
            lock.unlock();
        }
    }
    
    public int size() {
        return this.size;
    }

    @Override
    public Iterator<Session> iterator() {
        return new SSIterator();
    }
    
    public class SSIterator implements Iterator<Session> {
        private int itIdx;
        private int curIdx;
        
        @Override
        public boolean hasNext() {
            while(itIdx < capacity) {
                if(sessions[itIdx] != null) return true;
                ++ itIdx;
            }
            return false;
        }

        @Override
        public Session next() {
            Session ret = sessions[itIdx];
            curIdx = itIdx;
            ++ itIdx;
            return ret;
        }

        @Override
        public void remove() {
            SessionSet.this.remove(curIdx);
        }
    }

}
