package com.x.game.cmd;

import com.x.game.player.Player;
import com.x.game.player.action.PlayerReqAction;
import com.x.net.codec.IMessage;


/**
 * 通过player.queue调用
 * @author 
 *
 */
public abstract class QueuedCommand<T extends Player> extends PlayerCommand {
    
    @Override
    protected final void execute0(Player player, IMessage req) throws Exception {
        new PlayerReqAction(player, req, this, player.queue()).checkin();
    }

    @SuppressWarnings("unchecked")
    public final void execute(Player player, IMessage req) throws Exception {
        exec((T) player, req);
    }

    protected abstract void exec(T player, IMessage req) throws Exception;
    
}
