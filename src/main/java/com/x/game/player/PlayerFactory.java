package com.x.game.player;

import com.x.action.ActionQueue;

public interface PlayerFactory {

    Player newPlayer(long playerId, ActionQueue queue);

}
