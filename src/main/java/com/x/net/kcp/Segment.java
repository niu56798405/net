package com.x.net.kcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.ReferenceCountUtil;

/**
 * SEGMENT
 */
public class Segment
{

  public int conv = 0;
  public byte cmd = 0;
  public int frg = 0;
  public int wnd = 0;
  public int ts = 0;
  public int sn = 0;
  public int una = 0;
  public int resendts = 0;
  public int rto = 0;
  public int fastack = 0;
  public int xmit = 0;
  public ByteBuf data;

  public Segment(int size)
  {
    if (size > 0)
    {
      this.data = PooledByteBufAllocator.DEFAULT.buffer(size);
    }
  }

  /**
   * encode a segment into buffer
   *
   * @param buf
   * @return
   */
  public int encode(ByteBuf buf)
  {
    int off = buf.writerIndex();
    buf.writeIntLE(conv);
    buf.writeByte(cmd);
    buf.writeByte(frg);
    buf.writeShortLE(wnd);
    buf.writeIntLE(ts);
    buf.writeIntLE(sn);
    buf.writeIntLE(una);
    buf.writeIntLE(data == null ? 0 : data.readableBytes());
    return buf.writerIndex() - off;
  }

  /**
   * 释放内存
   */
  public void release()
  {
    if (this.data != null )
    {
        ReferenceCountUtil.release(data);
    }
  }
}
