package com.x.game.cmd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.x.game.PlayerLoader;
import com.x.game.player.Player;
import com.x.injection.Inject;
import com.x.net.cmd.Command;
import com.x.net.codec.IMessage;
import com.x.net.session.Session;

public abstract class PlayerCommand implements Command {
    
    protected static Logger logger = LoggerFactory.getLogger(PlayerCommand.class);
    
    @Inject
    private PlayerLoader playerLoader;
    
    @Override
    public final void execute(Session session, IMessage req) throws Exception {
        long playerId = req.getId();
       
        Player player = playerLoader.getPlayer(playerId);
        if(player != null) {
            execute0(player, req);
        } else {
            logger.error("Player not found [" + playerId + "]");
        }
    }
    
    //for Queued Or Direct
    protected abstract void execute0(Player player, IMessage req) throws Exception;

    //for outter exec
    public abstract void execute(Player player, IMessage req) throws Exception;

}
