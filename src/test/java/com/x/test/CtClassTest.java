package com.x.test;

import org.junit.Ignore;

import com.x.game.protocol.simple.SimpleProto.IntMsg;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
@Ignore
public class CtClassTest {
	public static void main(String[] args) throws Exception {
		IntMsg.Builder bu = IntMsg.newBuilder();
		bu.setValue(99);
		IntMsg num = bu.build();
		Class<IntMsg> clazz = IntMsg.class;
		CtClass ctClass = ClassPool.getDefault().get(clazz.getName());
		CtMethod declaredMethod = ctClass.getDeclaredMethod("par");
		 
	}
}
