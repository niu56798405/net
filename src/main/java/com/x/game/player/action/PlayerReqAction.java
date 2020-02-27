package com.x.game.player.action;

import com.x.action.Action;
import com.x.action.ActionQueue;
import com.x.game.cmd.PlayerCommand;
import com.x.game.player.ExceptionHandler;
import com.x.game.player.Player;
import com.x.injection.Inject;
import com.x.injection.Prototype;
import com.x.net.codec.IMessage;

/**
 * 通过player task queue 执行的 request command
 * @author 
 */
@Prototype
public class PlayerReqAction extends Action {
    
    @Inject
    private ExceptionHandler handler;
    
    private Player player;
    
    private IMessage req;
    
    private PlayerCommand cmd;
    
    public PlayerReqAction(Player player, IMessage req, PlayerCommand cmd, ActionQueue queue) {
        super(queue);
        this.player = player;
        this.req = req;
        this.cmd = cmd;
    }

    @Override
    protected void exec() {
        try {
            cmd.execute(player, req);
        } catch (Exception ex) {
            handler.onException(player.getPlayerId(), cmd, req, ex);
        }
    }

    @Override
    public String toString() {
        return "cmd=" + cmd;
    }
    
    protected Class<?> getClazz() {
    	return cmd.getClass();
    }

}
