package com.x.test;

import com.x.event.Event;
import com.x.event.Events;
import com.x.event.Listener;

public class EventTest {
	public static void main(String[] args) {
		Events events = new Events();
		
		Listener<Event> listener = Listener.of(1, 1, (v) -> {System.out.println(v);});
				
		events.regist(listener);
		events.notify(new Event(1));
		
		
		
	}
}
