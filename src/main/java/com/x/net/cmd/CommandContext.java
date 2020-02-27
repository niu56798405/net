package com.x.net.cmd;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.x.injection.ApplicationContext;
import com.x.injection.ApplicationContext.Finalizer;
import com.x.injection.ApplicationContext.Initializer;
import com.x.injection.Bean;
import com.x.injection.Injection;

/**
 * 命令集合 遍历所有文件,找出含有@cmd annotation的命令类
 * @author 
 */
@Bean
public class CommandContext implements Finalizer {
    
    private Map<Short, Command> cmds;
    private Command defaultCmd;
    
    public CommandContext() {
        cmds = new HashMap<>();
    }

    public void registDefaultCmd(Command command) {
    	this.defaultCmd = command;
    }
    
    public void registCmd(short code, Command command) {
        this.cmds.put(code, command);
    }

    public Command get(short cmdCode) {
        Command cmd = cmds.get(cmdCode);
        return cmd == null ? defaultCmd : cmd;//可以配置一个默认的cmd
    }

    public int size() {
        return cmds.size();
    }

    @Override
    public void finalize(List<Class<?>> clazzes) {
        for (Class<?> clazz : clazzes) registCommand(clazz);
    }
    
    public void registCommand(Class<?> clazz) {
        Cmd ann = clazz.getAnnotation(Cmd.class);
        if(ann != null && !Modifier.isAbstract(clazz.getModifiers()) && !Modifier.isInterface(clazz.getModifiers())) {
            registCmd(ann.value(), (Command) Injection.inject(newInstance(clazz)));
        }
    }

    private Command newInstance(Class<?> clazz) {
        try {
            CommandBuilder builder = ApplicationContext.fetchBean(CommandBuilder.class);
            return  (builder == null ? (Command) clazz.newInstance() : builder.build(clazz));
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException(clazz.getName(), e);
        }
    }
    
}