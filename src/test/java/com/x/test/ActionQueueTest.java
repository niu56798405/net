package com.x.test;

import org.junit.Ignore;

import com.x.action.Action;
import com.x.action.ActionExecutor;
import com.x.action.ActionExecutors;
import com.x.action.ActionQueue;

@Ignore
public class ActionQueueTest {
    
    public static void main(String[] args) {
        ActionExecutor executor = ActionExecutors.newCached("Test", 1, 5);
        ActionQueue qA = new ActionQueue(executor);
        ActionQueue qB = new ActionQueue(executor);
        	
        
        
//        
//        for (int i = 0; i < 10; i++) {
//            final int j = i;
//            ActionQueue actionQueue = new ActionQueue(executor);
//            (new Action(actionQueue) {
//                @Override
//                protected void exec() {
//                    System.out.println(j);
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).checkin();
//        }
//        executor.shutdown();
    }
    
    
    public static class A extends Action{

		@Override
		protected void exec() {
			
		}    	      
    }
       
}
