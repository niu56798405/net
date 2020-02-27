package com.x.game.cmd;

import com.x.game.player.ModularPlayer;
import com.x.game.player.Player;
import com.x.modular.Module;
import com.x.net.codec.IMessage;

/**
 * 直接的, 没有经过player.queue
 * @author 
 */
public abstract class DirectModularCmd<T extends ModularPlayer, V extends Module<?>> extends ModularCommand<T, V> {

    @Override
    protected final void execute0(Player player, IMessage req) throws Exception {
        execute(player, req);
    }
    
}
