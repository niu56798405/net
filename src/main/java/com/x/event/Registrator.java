package com.x.event;

import java.util.function.Consumer;

public interface Registrator {
    
    public void regist(Listener<? extends Event> listener);
    
    public default void regist(int group, int type, Consumer<? extends Event> acceptor) {
        regist(Listener.of(group, type, acceptor));
    }
    
    public default void regist(int type, Consumer<? extends Event> acceptor) {
        regist(0, type, acceptor);//use default group [0]
    }
    
    public void unregist(int group);
    
}
