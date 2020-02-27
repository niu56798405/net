package com.x.game.cmd;

import com.x.game.player.ModularPlayer;
import com.x.game.player.Player;
import com.x.game.player.action.PlayerReqAction;
import com.x.modular.Module;
import com.x.net.codec.IMessage;


/**
 * 通过player.queue调用
 * @author 
 *
 */
public abstract class QueuedModularCmd<T extends ModularPlayer, V extends Module<?>> extends ModularCommand<T, V> {

    @Override
    protected final void execute0(Player player, IMessage req) throws Exception {
        new PlayerReqAction(player, req, this, player.queue()).checkin();
    }
    
}
