package com.x.game.cmd;

import com.x.game.player.ModularPlayer;
import com.x.game.player.Player;
import com.x.modular.ModularBridge;
import com.x.modular.ModularBridge.Type;
import com.x.modular.ModularInject;
import com.x.modular.Module;
import com.x.net.codec.IMessage;

/**
 * module 入口
 * @author 
 *
 * @param <T>
 * @param <V>
 */
public abstract class ModularCommand<T extends ModularPlayer, V extends Module<?> > extends PlayerCommand {
	
    @ModularBridge(Type.ENTRY)
    public void execute(Player player, IMessage req) throws Exception {
        //for dynamic override
        //bridge to Descriptor method
    }
    
    @ModularBridge(Type.INTERCEPT)
    public void execute(T player, V module, IMessage req) throws Exception {
        exec(player, module, req);
    }

    @ModularBridge(Type.BRIDGE)
    public abstract void exec(T player, @ModularInject V module, IMessage req) throws Exception;
    
}
