package com.x.test;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

public class MainTest {
	public static void main(String[] args) throws Exception {
		ClassPool cp = ClassPool.getDefault();
        CtClass cc = cp.get("com.x.test.JavassitTest");
        CtMethod m = cc.getDeclaredMethod("print");
        m.setBody("{System.out.println(\"shit\");}");
        m.insertBefore("System.out.println(\"hello world\");");
        cc.toClass();
    	JavassitTest t = new JavassitTest();
    	t.print();
	}
}
