package com.x.game.player.callable;

import com.x.action.Action;
import com.x.game.player.Callable;
import com.x.game.player.Player;

public abstract class QueuedCallable<T extends Player> implements Callable<T>{

    @Override
    public final void call(final T player) {
        new Action(player.queue()) {
            @Override
            protected void exec() {
                QueuedCallable.this.exec(player);
            }
        }.checkin();
    }

    protected abstract void exec(T player);

}
