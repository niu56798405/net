package com.x.net;

import com.x.net.codec.IMessage;

public interface MessageSender {
    
    void sendMessage(long sessionId, IMessage message);

}
