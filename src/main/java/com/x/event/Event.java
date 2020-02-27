package com.x.event;

public class Event {
    
    public final int type;
    public Event(int type) {
        super();
        this.type = type;
    }
    
    
    public <T extends Event> T cast() {
		return (T)this;
	}

}
