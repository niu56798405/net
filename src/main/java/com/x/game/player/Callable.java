package com.x.game.player;


/**
 * 
 * @author 
 *
 */
public interface Callable<T extends Player> {

    public void call(T player);
    
}
