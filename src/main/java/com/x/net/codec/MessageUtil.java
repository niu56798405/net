package com.x.net.codec;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;

public class MessageUtil {
	
	public static Message decode(ByteBuf buff){
		Message message = Message.build();
		message.readHeader(buff);
		message.readParams(buff);
	    message.readBody(buff);
	    ReferenceCountUtil.release(buff);
		return message;
	}
	
	public static ByteBuf encode(IMessage msg, ByteBuf buf){
	    msg.writeHeader(buf);
	    msg.writeParams(buf);
	    msg.writeBody(buf);
		return buf;
	} 
}
