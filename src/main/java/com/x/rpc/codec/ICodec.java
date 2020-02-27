package com.x.rpc.codec;

public interface ICodec {
	byte[] encode(Object object);
	
	<T> T decode(byte[] byteArray);
}
