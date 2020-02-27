package com.x.net;

import com.x.injection.Combine;
import com.x.net.cmd.Command;
import com.x.net.codec.IMessage;
import com.x.net.session.Session;

@Combine
public interface LifecycleListener {
    
    public void onSessionRegister(Session session);
    
    public void onMessageRecieve(Session session, IMessage message);
    
    public void onMessageSending(Session session, IMessage message);
    
    public void onSessionUnRegister(Session session);

    public void onCmdException(Session session, Command cmd, IMessage req, Throwable ex);

}
