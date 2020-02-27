package com.x.test;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.util.HotSwapAgent;

public class HotswapTest {
	 public static void main(String[] args) throws Exception {
	        // run java -javaagent:hotswap.jar javassist.HotswapTest
	        new HotswapTest(HotswapTest.class.getName()).testHotswap();
	    }

	    public HotswapTest(String name) {
	    }

	

	    public void testHotswap() throws Exception {
	   

	        Foo f = new Foo();
	        System.out.println( f.foo());

	        ClassPool cp = ClassPool.getDefault();
	        CtClass clazz = cp.get(Foo.class.getName());
	        CtMethod m = clazz.getDeclaredMethod("foo");
	        clazz.removeMethod(m);
	        clazz.addMethod(CtNewMethod.make("public int foo() { return 2; }", clazz));
//	        HotSwapAgent.redefine(Foo.class, clazz);
	        Foo g = new Foo();
	        System.out.println(g.foo());
	        
	        System.out.println(f.foo());
	        System.out.println("Foo#foo() = " + g.foo());
	    }
	    
	    public static class Foo {
	        public int foo() { return 1; }
	    }  
	    
}
