package com.x.net.codec;



/**
 * 消息构建
 * @author 
 */
public abstract class MessageBuilder {
    
    public IMessage build() {
        return build((short) 0);
    }

    public abstract IMessage build(short code);

}