package com.x.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.x.game.protocol.simple.SimpleProto.RpcMsg;
import com.x.game.protocol.simple.SimpleProto.TestMsg;
import com.x.game.protocol.simple.SimpleProto.TestMsg.Builder;
import com.x.util.ProtostuffCodec;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

public class JavassitTest {
	
	public static Map<Class<?>, Parser<?>> map = new HashMap<>();
	
	static{
		System.out.println("init >>>>");
	}
	
	public void print(){
		System.out.println("hello");
	}
	
	public static void main(String[] args) throws Exception {
		List<Integer> list = new LinkedList<>();
		list.add(1);
		byte[] e = ProtostuffCodec.encode(list);
		list = ProtostuffCodec.decode(e);
		
		Map<Integer, String> map = new HashMap<>();
		map.put(1, "99");
		
		byte[] encode = ProtostuffCodec.encode(map);
		Map<Integer, String> decode = ProtostuffCodec.decode(encode);
		String x = decode.get(1);
		System.out.println(x);

		
		RpcMsg.Builder builder = RpcMsg.newBuilder();
		builder.setClassName(Test.class.getName());
		builder.setMethodName("testProto");
		builder.setCode(999);
		Builder newBuilder = TestMsg.newBuilder();
		newBuilder.addList(1);
		newBuilder.addList(998);
		newBuilder.addList(889);
		newBuilder.setCode(56);
		newBuilder.setName("test");
		for(int i = 0;i<100 ;i++){
			builder.addParams(newBuilder);
		}
		byte[] byteArray = builder.build().toByteArray();
		System.out.println("protocol size:" + byteArray.length);
		
		Method method = parseFrom(RpcMsg.class);
		int times = 100000;
		long s1 = System.currentTimeMillis();
		for(int i = 0;i<times ;i++){
			Object parse = method.invoke(null, byteArray);
		}
		long e1 = System.currentTimeMillis();
		System.out.println(e1 - s1);
		
		AbstractParser<RpcMsg> createParser = createParser(RpcMsg.class);
		long s20 = System.currentTimeMillis();
		for(int i = 0;i<times ;i++){
			RpcMsg parse = createParser.parseFrom(byteArray);
		}
		long e20 = System.currentTimeMillis();
		System.out.println("e20:" + (e20 - s20));
		Parser<RpcMsg> buildParser = buildParser(RpcMsg.class);
		long s2 = System.currentTimeMillis();
		for(int i = 0;i<times ;i++){
			RpcMsg parse = buildParser.parse(byteArray);
		}
		long e2 = System.currentTimeMillis();
		System.out.println("e2:" + (e2 - s2));

		long s3 = System.currentTimeMillis();
		for(int i = 0;i<times ;i++){
			Object parse = p(byteArray);
		}
		long e3 = System.currentTimeMillis();
		System.out.println(e3 - s3);
		byteArray = ProtostuffCodec.encode(builder.build());
		System.out.println("stuff size:" + byteArray.length);

		long s4 = System.currentTimeMillis();
//		for(int i = 0;i<times ;i++){
//			Object parse = SerializationUtils.deserializer(byteArray, RpcMsg.class);
//		}
		
		
		long e4 = System.currentTimeMillis();
		System.out.println(e4 - s4);
		
		
		
		ProtocolTest protocolTest = new ProtocolTest();
		protocolTest.setClazzName("com.x.ProtocolTest");
		protocolTest.setMethodName("metestAdd");
		Object[] object = new Object[100];
//		List<Integer> list = new LinkedList<>();
		
		list.add(1000);
		list.add(1001);
		list.add(1002);
		
		for(int i = 0; i < 100 ;i++){
			object[i] = list;
		}
				
		protocolTest.setObjects(object);
		
		byteArray = ProtostuffCodec.encode(protocolTest);
		System.out.println("stuff size:" + byteArray.length);

		long s5 = System.currentTimeMillis();
		for(int i = 0;i<times ;i++){
			Object parse = ProtostuffCodec.decode(byteArray, ProtocolTest.class);
		}
		
		
		long e5 = System.currentTimeMillis();
		System.out.println(e5 - s5);
		
		
		
		byteArray = JSONObject.toJSONBytes(protocolTest);
		System.out.println("json size:" + byteArray.length);

		long s6 = System.currentTimeMillis();
		for(int i = 0;i<times ;i++){
			Object parse = JSONObject.parseObject(byteArray, ProtocolTest.class);
		}
		
		
		long e6 = System.currentTimeMillis();
		System.out.println(e6 - s6);
		
		
	}
	
	public static Object p(byte[] byteArray) throws InvalidProtocolBufferException{
		Object parse = RpcMsg.parseFrom(byteArray);
		return parse;
	}
	
	public static Method parseFrom(Class<?> clazz) throws Exception {
       
        Method method = clazz.getMethod("parseFrom", byte[].class);
        return method;
   }
	
	
	public static <T>Parser<T> buildParser(Class<T> clazz) throws Exception{
		Parser parse = map.get(clazz);
		if (parse != null) {
			return parse;
		}
		String interfaceName = clazz.getCanonicalName();
 	   	ClassPool mPool = ClassPool.getDefault();
        CtClass mCtc = mPool.makeClass(interfaceName + "_proxy_" + "Parser");
        
        mCtc.addInterface(mPool.getCtClass(Parser.class.getName()));        
        StringBuilder sb = new StringBuilder();
        sb.append("public Object parse(byte[] array) {");
        sb.append("return " + interfaceName + ".parseFrom(array);");
        sb.append("}");
              
        CtMethod make = CtMethod.make(sb.toString(), mCtc);
		mCtc.addMethod(make);
        
      
        Class<?> proxyClass = mCtc.toClass();
        mCtc.detach();//释放缓存
        Object instance = proxyClass.newInstance();
		parse = (Parser<T>)instance;
        map.put(clazz, parse);				
		return parse;
	}
	
	public interface Parser<T>{
		public <T> T parse(byte[] array);
	}
	
	
	   private static <T extends MessageLite> AbstractParser createParser(Class<T> pbClass) {
	        return new com.google.protobuf.AbstractParser<T>() {
	            public T parsePartialFrom(
	                com.google.protobuf.CodedInputStream input,
	                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
	                throws com.google.protobuf.InvalidProtocolBufferException {
	                try {
	                    Constructor<T> constructor;
	                    constructor = pbClass
	                        .getDeclaredConstructor(com.google.protobuf.CodedInputStream.class,
	                            com.google.protobuf.ExtensionRegistryLite.class);
	                    constructor.setAccessible(true);
	                    return constructor.newInstance(input, extensionRegistry);
	                } catch (Exception e) {
	                    e.printStackTrace();
	                }
	                return null;
	            }
	        };
	    }
	
}
