package com.x.action;

/**
 * 
 * @author 
 *
 */
public interface ActionExecutor {
    
    public ActionQueue defaultQueue();
    
    public void delayCheck(DelayAction action);

    public void execute(Runnable action);
    
    public void shutdown();
    
}
