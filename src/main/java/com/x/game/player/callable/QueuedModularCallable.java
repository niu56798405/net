package com.x.game.player.callable;

import com.x.action.Action;
import com.x.game.player.ModularPlayer;
import com.x.modular.Module;

public abstract class QueuedModularCallable<T extends ModularPlayer, V extends Module<?>> extends ModularCallable<T, V> {

    @Override
    protected final void call(final T player, final V module) {
        new Action(player.queue()) {
            @Override
            protected void exec() {
                QueuedModularCallable.this.exec(player, module);
            }
        }.checkin();
    }

    protected abstract void exec(T player, V module);

}
