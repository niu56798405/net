package com.x.game.player;

import com.x.game.PlayerLoader;
import com.x.injection.Bean;
import com.x.injection.Inject;

@Bean(backup=true)
public class DefaultPlayerLoader implements PlayerLoader {
    
    @Inject(nullable=true)
    private PlayerContext playerCtx;
    
    @Override
    public boolean exists(long playerId) {
        return playerCtx.exists(playerId);
    }
    
    @Override
    public <T extends Player> T getPlayer(long playerId) {
        return playerCtx.getPlayerWithLoad(playerId);
    }



    
}
