package com.x.test.disruptor;

import com.x.action.Action;
import com.x.action.ActionQueue;

public class TAction extends Action{
	public long index;
	public long time;
	
	
	public TAction(ActionQueue queue) {
		super(queue);
	}


	@Override
	protected void exec() {

		if (index == DisruptorTest.size - 1) {
			System.out.println(Thread.currentThread());

			System.out.println("Action:>>>>>>>>>>  time : " + (System.currentTimeMillis() - time) +"index :" + index);
		}
	}
	
} 