package com.x.net.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.x.injection.Bean;
import com.x.net.MessageSender;
import com.x.net.codec.IMessage;

@Bean
public class SessionContext implements MessageSender {
    
    private ConcurrentHashMap<Long, Session> sessions;
    
    public SessionContext() {
        sessions = new ConcurrentHashMap<Long, Session>();        
    }
    
    public Session getSession(long sessionId) {
        return sessions.get(sessionId);
    }
    
    /**
     * @param id
     * @param channel
     */
    public Session registSession(long id, Session session) {
        return sessions.put(id, session.bind(id));
    }
    
    public void closeSession(long id) {
        closeSession(sessions.remove(id));
    }
    
    private void closeSession(Session session) {
        if(session != null)
            session.close();
    }
    
    public int sessionCount() {
        return this.sessions.size();
    }

    public void sendMessage(long sessionId, IMessage message) {
        Session session = sessions.get(sessionId);
        if(session != null) {
            session.sendMessage(message);
        }
    }
    
    /**
     * send message to all sessions
     * @param message
     */
    public void sendMessage(IMessage message) {
    	for (Session session : new ArrayList<>(sessions.values())) {
    		session.sendMessage(message);
    	}
    }
    
    public Collection<Session> getAllSession(){
    	return sessions.values();
    }

}
