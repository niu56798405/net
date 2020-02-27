package com.x.test;

import java.util.concurrent.TimeUnit;

import org.junit.Ignore;

import com.x.action.Action;
import com.x.action.ActionChain;
import com.x.action.ActionExecutor;
import com.x.action.ActionExecutors;
import com.x.action.ActionQueue;

@Ignore
public class ActionChainTest {
    
    public static void main(String[] args) throws InterruptedException {
        ActionExecutor executor = ActionExecutors.newFixed("T", 1);
        ActionQueue queue = new ActionQueue(executor);
        ActionChain chain = new ActionChain();
        
        chain.append(new Action(queue) {
            @Override
            public void exec() {
                System.out.println("Ni Hao!!!");
            }
        });
        
        chain.append(new Action(queue) {
            @Override
            public void exec() {
                System.out.println("Say Hello!!!");
            }
        });
        chain.checkin();
        
        TimeUnit.MILLISECONDS.sleep(50);
        executor.shutdown();
    }

}
