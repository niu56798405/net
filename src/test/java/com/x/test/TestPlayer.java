package com.x.test;

import org.junit.Ignore;

import com.x.action.ActionQueue;
import com.x.game.player.ModularPlayer;
import com.x.modular.ModularInject;
import com.x.modular.ModularType;

@Ignore
public class TestPlayer extends ModularPlayer {
    
    @ModularInject
    public TSharablePlayer player;

	public TestPlayer(long playerId, ActionQueue queue) {
		super(playerId, queue);
	}

    @Override
    public boolean unloadable(ModularType type) {
        return type == ModularType.TRANSIENT;
    }

    @Override
    public boolean isOnline() {
        return true;
    }

}
