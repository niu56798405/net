package com.x.net;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.x.net.codec.IMessage;
import com.x.net.session.Session;

public class MessageGroup {
	
	private Map<Long, Session> sessions = new ConcurrentHashMap<Long, Session>();
	
	
	public void sendMessage(IMessage msg) {
		for(Session session : sessions.values()){
			session.sendMessage(msg);
		}
	}
	
	public void add(Session element) {		
		sessions.put(element.id(), element);
	}

	
	public void remove(Session element) {
		if(element != null){
			sessions.remove(element.id());
		}		
	}
	
	public void clear(){
		sessions.clear();
	}

}
