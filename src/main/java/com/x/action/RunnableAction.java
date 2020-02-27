package com.x.action;

public class RunnableAction extends Action {

    private final Runnable runnable;
    
    public RunnableAction(ActionQueue queue, Runnable runnable) {
        super(queue);
        this.runnable = runnable;
    }

    @Override
    protected void exec() {
        runnable.run();
    }

}
