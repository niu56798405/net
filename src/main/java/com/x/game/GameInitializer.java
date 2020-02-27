package com.x.game;

import java.lang.reflect.Constructor;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.x.action.ActionExecutor;
import com.x.action.ActionExecutors;
import com.x.action.ActionQueue;
import com.x.game.cmd.ModularCommand;
import com.x.game.player.Player;
import com.x.game.player.PlayerContext;
import com.x.game.player.PlayerFactory;
import com.x.injection.ApplicationContext;
import com.x.injection.ApplicationContext.Initializer;
import com.x.modular.ModularEnigne;
import com.x.modular.ModuleContainer;
import com.x.modular.code.GenEX;
import com.x.modular.code.GenMF;
import com.x.net.cmd.Command;
import com.x.net.cmd.CommandBuilder;

public abstract class GameInitializer {
    
    private static final Logger logger = LoggerFactory.getLogger(GameInitializer.class);
    protected GameInitializer() {
    }
    
    public static void initialize(Class<? extends Player> assemble, final int threads) {
        GameInitializer initializer = newInitializer(assemble);
        ApplicationContext.registBean(new Initializer() {
            @Override
            public void initialize(List<Class<?>> clazzes) {
                initializer.initialize0(assemble, threads);
                logger.info("Load compelete modular and queue threads[{}]", threads);
            }
        });
    }
    
    public static void initialize(Class<? extends Player> assemble){
    	initialize(assemble, Runtime.getRuntime().availableProcessors() * 2);
    } 
    
    
    protected static GameInitializer newInitializer(Class<? extends Player> assemble) {
        if(ModuleContainer.class.isAssignableFrom(assemble)) {
            ModularEnigne.initialize(assemble);
            return new ModularInitializer();
        } else {
            return new NormalInitializer(assemble);
        }
    }
    
    protected void initialize0(Class<? extends Player> assemble, final int threads) {
        ActionExecutor executor = ActionExecutors.newCached("queues", threads);//max: 2*threads
        PlayerFactory factory = newPlayerFactory();
        PlayerContext context = new PlayerContext(executor, factory);
        
        ApplicationContext.registBean(PlayerFactory.class, factory);
        ApplicationContext.registBean(PlayerContext.class, context);
        ApplicationContext.registBean(CommandBuilder.class, newCommandBuilder());
    }

    protected abstract PlayerFactory newPlayerFactory();
    
    protected abstract CommandBuilder newCommandBuilder();

    static class NormalInitializer extends GameInitializer {
        Constructor<? extends Player> constructor;
        public NormalInitializer(Class<? extends Player> assemble) {
            try {
                constructor = assemble.getConstructor(long.class, ActionQueue.class);
            } catch (Throwable e) {
                throw new IllegalArgumentException(e);
            }
        }
        @Override
        protected PlayerFactory newPlayerFactory() {
            return (playerId, queue) -> {
                try {
                    return constructor.newInstance(playerId, queue);
                } catch (Throwable e) {
                    throw new IllegalArgumentException(e);
                }
            };
        }
        @Override
        protected CommandBuilder newCommandBuilder() {
            return clazz -> {
                try {
                    return (Command) clazz.newInstance();
                } catch (Throwable e) {
                    throw new IllegalArgumentException(clazz.getName(), e);
                }
            };
        }
    }
    
    static class ModularInitializer extends GameInitializer {
        @Override
        protected PlayerFactory newPlayerFactory() {
            return GenMF.generate(PlayerFactory.class);
        }
        @Override
        protected CommandBuilder newCommandBuilder() {
            return clazz->{
                try {
                    return (Command) (ModularCommand.class.isAssignableFrom(clazz) ? GenEX.generate(clazz, ModularCommand.class) : clazz.newInstance());
                } catch (Throwable e) {
                    throw new IllegalArgumentException(clazz.getName(), e);
                }
            };
        }
    }
    
}
