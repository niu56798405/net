package com.x.game.cmd;

import com.x.game.player.Player;
import com.x.net.codec.IMessage;

/**
 * 直接的, 没有经过player.queue
 * @author 
 */
public abstract class DirectCommand<T extends Player> extends PlayerCommand {
    
    @Override
    protected final void execute0(Player player, IMessage req) throws Exception {
        execute(player, req);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public final void execute(Player player, IMessage req) throws Exception {
        exec((T) player, req);
    }

    protected abstract void exec(T player, IMessage req) throws Exception;
    
}
