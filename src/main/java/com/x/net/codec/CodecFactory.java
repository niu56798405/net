package com.x.net.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

public class CodecFactory {

    public static final String STRICT_NAME = "message.strict";
    
    private final MessageBuilder builder;
    private final boolean strict;
    
    public CodecFactory(MessageBuilder builder) {
        this(builder, Boolean.parseBoolean(System.getProperty(STRICT_NAME, "false")));
    }
    public CodecFactory(MessageBuilder builder, boolean strict) {
        this.builder = builder;
        this.strict = strict;
    }
    
    public MessageDecoder newDecoder() {
        return strict ? newStrictDecoder() : new MessageDecoder(builder);
    }
    private MessageDecoder newStrictDecoder() {
        return new MessageDecoder(builder){
            protected void decode(ChannelHandlerContext ctx, IMessage message) {
                CodecFactory.this.decode(ctx, message);
            }
        };
    }
    
    public MessageEncoder newEncoder() {
        return strict ? newStrictEncoder() : new MessageEncoder();
    }
    private MessageEncoder newStrictEncoder() {
        return new MessageEncoder(){
            @Override
            protected void encode(ChannelHandlerContext ctx, IMessage message) {
                CodecFactory.this.encode(ctx, message);
            }
        };
    }
    
    
    /*--------------------strict codec-----------------------*/
    
    private AttributeKey<byte[]> ENCRYPT_CIPHER = AttributeKey.valueOf("ENCRYPTION");
    private AttributeKey<byte[]> DECRYPT_CIPHER = AttributeKey.valueOf("DECRYPTION");
    
    protected void encode(ChannelHandlerContext ctx, IMessage message) {
        byte[] cipher = getCipher(ctx, ENCRYPT_CIPHER);
        
        short header = headerCodec(message, cipher);
        short code = codeCodec(message, cipher);
        
        setCipher(cipher, message);
        
        message.setHeader(header);
        message.setCode(code);
    }
    
    protected void decode(ChannelHandlerContext ctx, IMessage message) {
        byte[] cipher = getCipher(ctx, DECRYPT_CIPHER);
        
        short header = headerCodec(message, cipher);
        short code = codeCodec(message, cipher);
        
        message.setHeader(header);
        message.setCode(code);
        
        setCipher(cipher, message);
    }

    protected short headerCodec(IMessage message, byte[] cipher) {
        return codec(message.getHeader(), cipher[0], cipher[1]);
    }
    
    protected short codeCodec(IMessage message, byte[] cipher) {
        return codec(message.getCode(), cipher[2], cipher[3]);
    }
    
    protected short codec(short src, byte cipher1, byte cipher2) {
        return (short) (src ^ (((cipher1 & 0xFF) << 8) | (cipher2 & 0xFF)) & 0xFFFF);
    }

    protected void setCipher(byte[] cipher, IMessage message) {
        int bodyLen = message.getBodyLen();
        if(bodyLen > 4) {
            byte[] body = message.getBody();
            cipher[0] = body[bodyLen-1];
            cipher[1] = body[bodyLen-2];
            cipher[2] = body[bodyLen-3];
            cipher[3] = body[bodyLen-4];
        } else {
            cipher[0] = (byte) (message.getCode() >> 8 & 0XFF);
            cipher[1] = (byte) (message.getCode() & 0XFF);
            cipher[2] = (byte) (message.getHeader() >> 8 & 0XFF);
            cipher[3] = (byte) (message.getHeader() & 0XFF);
        }
    }

    protected byte[] getCipher(ChannelHandlerContext ctx, AttributeKey<byte[]> key) {
        byte[] cipher = ctx.attr(key).get();
        if(cipher == null) {
            cipher = new byte[4];
            for (int i = 0; i < 4; i++) {
                cipher[i] = (byte) i;
            }
            ctx.attr(key).set(cipher);
        }
        return cipher;
    }
    
}
