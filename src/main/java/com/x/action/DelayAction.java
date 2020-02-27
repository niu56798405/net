package com.x.action;


public abstract class DelayAction extends Action {
    
    long execTime;
    
    volatile boolean isCancelled;
    
	public DelayAction(ActionQueue queue, int delay) {
	    super(queue);
	    this.initialize(createTime, delay);
	}
	
	public DelayAction(ActionQueue queue, long curTime, int delay) {
		super(queue);
		this.initialize(curTime, delay);
	}

	@Override
    protected boolean runable() {
	    return !isCancelled;
    }

    private void initialize(long curTime, int delay) {
        this.isCancelled = false;
        this.createTime = curTime;
        this.execTime = delay > 0 ? (curTime + delay) : 0;
	}

	@Override
    public void checkin() {
	    if(this.execTime == 0) {//don`t need delay
	        queue.checkin(this);
	    } else {
	        queue.checkinDelayAction(this);
	    }
    }
	
	public void cancel() {
	    this.isCancelled = true;
	}
	
	public void recheckin(int delay) {
		this.recheckin(System.currentTimeMillis(), delay);
	}
	
	public void recheckin(long curTime, int delay) {
		initialize(curTime, delay);
		checkin();
	}

    public boolean tryExec(long curTime) {
        if(isCancelled) {
            return true;
        }
        
		if(curTime >= execTime) {
			createTime = curTime;
			getActionQueue().checkin(this);
			return true;
		}
		return false;
	}
}
