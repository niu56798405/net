package com.x.net.cmd;

public interface CommandBuilder {
    Command build(Class<?> clazz);
}
