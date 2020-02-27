package com.x.game.player;

import com.x.injection.Combine;
import com.x.net.cmd.Command;
import com.x.net.codec.IMessage;

@Combine
public interface ExceptionHandler {
    
    public void onException(long playerId, Command cmd, IMessage req, Throwable throwable);

}
