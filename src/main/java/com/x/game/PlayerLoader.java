package com.x.game;

import com.x.game.player.Player;

public interface PlayerLoader {
    
    public boolean exists(long playerId);
    
    public <T extends Player> T getPlayer(long playerId);
        
}
