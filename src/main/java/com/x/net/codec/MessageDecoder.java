package com.x.net.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 解码
 * @author 
 */
public class MessageDecoder extends ByteToMessageDecoder {
    
    private final MessageBuilder builder;
    
    public MessageDecoder(MessageBuilder builder) {
        this.builder = builder;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buff, List<Object> out) throws Exception {
        if (buff.readableBytes() < Message.HDR_SIZE) // 不够字节忽略
            return;

        buff.markReaderIndex();
        IMessage message = builder.build();
        message.readHeader(buff);
        int len = message.getBodyLen() + message.getParamsLen();
        if (len > buff.readableBytes()) {
            buff.resetReaderIndex();
            return;
        }
        
        message.readParams(buff);
        message.readBody(buff);
        
        decode(ctx, message);
        
        out.add(message);
    }

    protected void decode(ChannelHandlerContext ctx, IMessage message) {
    }

}
