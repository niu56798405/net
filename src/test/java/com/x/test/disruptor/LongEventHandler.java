package com.x.test.disruptor;

import com.lmax.disruptor.EventHandler;

public class LongEventHandler implements EventHandler<LongEvent> {

	@Override
	public void onEvent(LongEvent event, long arg1, boolean arg2) throws Exception {
		if (event.getValue() == DisruptorTest.size - 1) {
			System.out.println("Event:>>>>>>>>>>  time : " + (System.currentTimeMillis() - event.time));
		}
				
	}

}
