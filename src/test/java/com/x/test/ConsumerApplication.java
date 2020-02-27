package com.x.test;

import java.util.List;

public class ConsumerApplication {
	private int a = 5;
	private List<Integer> list;
	private boolean bool;
	private Type type;
	private int[] intArray;
	
	
	public int[] getIntArray() {
		return intArray;
	}
	public void setIntArray(int[] intArray) {
		this.intArray = intArray;
	}
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	public int getA() {
		return a;
	}
	public void setA(int a) {
		this.a = a;
	}
	public List<Integer> getList() {
		return list;
	}
	public void setList(List<Integer> list) {
		this.list = list;
	}
	public boolean isBool() {
		return bool;
	}
	public void setBool(boolean bool) {
		this.bool = bool;
	}
	@Override
	public String toString() {
		return "ConsumerApplication [a=" + a + ", list=" + list + ", bool=" + bool + "]";
	}
	
	
	public enum Type{
		A,
		B,
		C		
	}	
}