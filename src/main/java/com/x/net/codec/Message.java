package com.x.net.codec;

import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import com.google.protobuf.MessageLite;
import com.x.tools.Strings;
import com.x.util.ProtostuffCodec;

/**
 * 前后端通信消 消息头+消息体, 消息体由protobuff生成
 * 
 * @author 
 */
public class Message implements IMessage {

    public static final byte[] EMPTY_BODY = new byte[0];

    public static final MessageBuilder BUIDLER = new MessageBuilder() {
        @Override
        public IMessage build(short code) {
            return Message.build(code);
        }
    };

    public static final short HDR_SIZE = 18;

    public static final short HDR_FLAG = 0X5362;
    
    public static final int MAX_BODY_LEN = Integer.MAX_VALUE;//0XFFFF;

    private short header;// 消息头标识, 从此字符开始为一个新的消息

    private short code;// 协议号,对应一个处理类(command)

    private long id;// 玩家在游戏中的唯一标识符

    private short version;// 前端版本号
    
    private short paramsLen;// 参数长度(len(short),content pairs)
    
    private int bodyLen;// 消息体长度, 指body的长度

    private byte[] body;// 消息体, 由具体业务逻辑来解析
    
    private Map<String, Param> params;

    private Message() {
        this.header = HDR_FLAG;
        this.body = EMPTY_BODY;
    }

    public short getHeader() {
        return header;
    }

    public void setHeader(short header) {
        this.header = header;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(short version) {
        this.version = version;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public void setCode(short code) {
        this.code = code;
    }

    public short getCode() {
        return this.code;
    }

    public int getParamsLen() {
        return paramsLen & MAX_BODY_LEN;
    }

    public void setParamsLen(short paramsLen) {
        this.paramsLen = paramsLen;
    }

    public int getBodyLen() {
        return bodyLen & MAX_BODY_LEN;
    }

    public void setBodyLen(int bodyLen) {
        this.bodyLen = bodyLen;
    }
    
    @Override
    public void addParam(String key, String value) {
        if(this.params == null) params = new HashMap<String, Param>();
        
        Param param = new Param(key, value);
        if(param.getLen() > Short.MAX_VALUE) {
            throw new IllegalArgumentException("Message param is too long 4 encoding!!!");
        }
        
        Param old = params.put(key, param);
        if(old != null)
            paramsLen -= old.getLen();
        
        paramsLen += param.getLen();
    }

    @Override
    public String getParam(String key) {
        if(params == null) return null;
        Param param = params.get(key);
        return param == null ? null : param.val;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.x.net.codec.IMessage#readHeader(io.netty.buffer.ByteBuf)
     */
    @Override
    public void readHeader(ByteBuf buff) {
        this.header = buff.readShort();
        // 16字节
        this.code = buff.readShort();
        this.id = buff.readLong();
        this.version = buff.readShort();
        this.paramsLen = buff.readShort();
        this.bodyLen = buff.readInt();
    }
    
    @Override
    public void readParams(ByteBuf buff) {
        int paramsLen = this.getParamsLen();
        if(paramsLen == 0) return;
        if(params == null) params = new HashMap<String, Param>();
        
        short readBytes = 0;
        while(readBytes < paramsLen) {
            byte[] key = readBytes(buff, buff.readShort());
            byte[] val = readBytes(buff, buff.readShort());
            
            Param param = new Param(key, val);
            params.put(param.key, param);
            
            readBytes += param.getLen();
        }
        if(readBytes != paramsLen) {
            //TODO Decode wrong
        }
    }

    private byte[] readBytes(ByteBuf buff, short len) {
        byte[] bytes = new byte[len];
        buff.readBytes(bytes);
        return bytes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.x.net.codec.IMessage#readBody(io.netty.buffer.ByteBuf)
     */
    @Override
    public void readBody(ByteBuf buff) {
        int len = this.getBodyLen();
        if (len > 0) {
            byte[] bytes = new byte[len];
            buff.readBytes(bytes);
            this.body = bytes;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.x.net.codec.IMessage#writeHeader(io.netty.buffer.ByteBuf)
     */
    @Override
    public void writeHeader(ByteBuf buff) {
        writeHeader0(buff, this.header, this.code, this.id);
    }

    private void writeHeader0(ByteBuf buff, short header, short code, long id) {
        buff.writeShort(header);
        buff.writeShort(code);
        buff.writeLong(id);
        
        buff.writeShort(this.version);
        buff.writeShort(this.paramsLen);
        buff.writeInt(this.bodyLen);
    }

    @Override
    public void writeParams(ByteBuf buff) {
        if(this.paramsLen == 0) return;
            
        for (Param param : params.values()) {
            buff.writeShort(param.keyBytes.length);
            buff.writeBytes(param.keyBytes);
            
            buff.writeShort(param.valBytes.length);
            buff.writeBytes(param.valBytes);
        }
    }
    /*
     * (non-Javadoc)
     * 
     * @see com.x.net.codec.IMessage#writeBody(io.netty.buffer.ByteBuf)
     */
    @Override
    public void writeBody(ByteBuf buff) {
        if (this.getBodyLen() > 0) {
            buff.writeBytes(this.body);
        }
    }

    public static Message build() {
        return new Message();
    }

    public static Message build(short code) {
        Message message = build();
        message.setCode(code);
        return message;
    }

    public static Message build(short code, long id) {
        Message message = build(code);
        message.setId(id);
        return message;
    }

    public static Message build(short code, MessageLite lite) {
        byte[] bytes = lite == null ? null : lite.toByteArray();
        return build(code, bytes);
    }

    public static Message build(short code, byte[] bytes) {
        Message message = build(code);
        if (bytes != null) {
            if(bytes.length > MAX_BODY_LEN) {
                throw new IllegalArgumentException("Message body is too long 4 encoding!!!");
            }
            message.setBodyLen(bytes.length);
            message.setBody(bytes);
        }
        return message;
    }
    
    public static Message build(short code, Object obj) {   	    	
    	return build(code, ProtostuffCodec.encode(obj));
    }
    
    
    
    static final AtomicInteger HURGE_ID = new AtomicInteger();
    public static int getNextHurgeId() {
        for (;;) {
            int current = HURGE_ID.get();
            int next = current + 1;
            if (next > Integer.MAX_VALUE) {
                next = 0;
            }
            if (HURGE_ID.compareAndSet(current, next)) {
                return next;
            }
        }
    }
    public static void buildHurge(short code, MessageLite lite, Consumer<Message> consumer) {
        byte[] bytes = lite == null ? null : lite.toByteArray();
        buildHurge(code, bytes, consumer);
    }
    
    public static void buildHurge(short code, byte[] bytes, Consumer<Message> consumer) {
        int len = bytes == null ? 0 : bytes.length;
        int max = MAX_BODY_LEN;
        if(len > max) {
            int id = getNextHurgeId();
            int cnt = (len / max + (len % max == 0 ? 0 : 1));
            int stidx = 0;
            while(stidx < len) {
                int edidx = Math.min(len, stidx + max);
                int slen = edidx - stidx;
                byte[] data = new byte[slen];
                System.arraycopy(bytes, stidx, data, 0, slen);
                Message message = Message.build(code, data);
                message.addParam("_id", String.valueOf(id));
                message.addParam("_cnt", String.valueOf(cnt));
                consumer.accept(message);
                stidx = edidx;
            }
        } else {
            consumer.accept(build(code, bytes));
        }
    }
    
    
    public IMessage copy(long id) {
        return new CopiedMessage(this, id);
    }
    
    private static class Param {
        public final byte[] keyBytes;
        public final String key;
        
        public final byte[] valBytes;
        public final String val;
        public Param(String key, String value) {
            this.key = key;
            this.keyBytes = Strings.getBytesUtf8(key);
            this.val = value;
            this.valBytes = Strings.getBytesUtf8(value);
        }
        public Param(byte[] keyBytes, byte[] valBytes) {
            this.key = Strings.newStringUtf8(keyBytes);
            this.keyBytes = keyBytes;
            
            this.val = Strings.newStringUtf8(valBytes);
            this.valBytes = valBytes;
        }
        public short getLen() {
            return (short) (2 + keyBytes.length + 2 + valBytes.length);
        }
        @Override
        public String toString() {
            return val;
        }
    }

    private static class CopiedMessage implements IMessage {
        private final Message message;
        private final long id;
        private short header;
        private short code;
        
        public CopiedMessage(Message message, long id) {
            this.message = message;
            this.id = id;
            this.header = message.header;
            this.code = message.code;
        }
        @Override
        public int getBodyLen() {
            return message.getBodyLen();
        }
        @Override
        public int getParamsLen() {
            return message.getParamsLen();
        }
        @Override
        public long getId() {
            return this.id;
        }
        @Override
        public byte[] getBody() {
            return message.body;
        }
        @Override
        public void readHeader(ByteBuf buff) {
            message.readHeader(buff);
        }
        @Override
        public void readBody(ByteBuf buff) {
            message.readBody(buff);
        }
        @Override
        public void writeHeader(ByteBuf buff) {
            message.writeHeader0(buff, this.header, this.code, this.id);
        }
        @Override
        public void writeBody(ByteBuf buff) {
            message.writeBody(buff);
        }
        @Override
        public void readParams(ByteBuf buff) {
            message.readParams(buff);
        }
        @Override
        public void writeParams(ByteBuf buff) {
            message.writeParams(buff);
        }
        @Override
        public void addParam(String key, String value) {
            message.addParam(key, value);
        }
        @Override
        public String getParam(String key) {
            return message.getParam(key);
        }
        @Override
        public short getHeader() {
            return this.header;
        }
        @Override
        public int getVersion() {
            return message.getVersion();
        }
        @Override
        public short getCode() {
            return this.code;
        }
        @Override
        public void setHeader(short header) {
            this.header = header;
        }
        @Override
        public void setCode(short code) {
            this.code = code;
        }
    }

}
