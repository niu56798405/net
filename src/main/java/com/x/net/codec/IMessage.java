package com.x.net.codec;

import io.netty.buffer.ByteBuf;


public interface IMessage {
    
    /**
     * body length
     * 包含在header里边
     */
    public int getBodyLen();
    
    public int getParamsLen();
    
    /**
     * 玩家的唯一标识符, 从PlayerContext里边取得玩家
     * @return
     */
    public long getId();
    
    public short getCode();
    
    public byte[] getBody();
    
    public void addParam(String key, String value);
    
    public String getParam(String key);
    
    public void readHeader(ByteBuf buff);
    
    public void readParams(ByteBuf buff);

    public void readBody(ByteBuf buff);

    public void writeHeader(ByteBuf buff);
    
    public void writeParams(ByteBuf buff);

    public void writeBody(ByteBuf buff);

    public short getHeader();
    
    int getVersion();
    
    void setHeader(short header);

    void setCode(short code);

}