package com.x.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class TestJunit {
	@Before
	public void first(){
		System.out.println(">>>>");
	}
	
	@Test
	public void a(){
		System.out.println(123456);
	}
	
	@After
	public void last(){
		System.out.println("end");
	}
	
}
