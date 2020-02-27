package com.x.action;

public abstract class CallableAction extends Action {

    private final Action callable;
    
    public CallableAction(ActionQueue queue, Action callable) {
        super(queue);
        this.callable = callable;
    }
    
    @Override
    public void done() {
        if(callable != null) callable.checkin();
    }

}
