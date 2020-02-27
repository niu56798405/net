package com.x.http.service.rest;

import com.x.http.service.Request;
import com.x.http.service.uri.PathMatcher;

public interface RestService {

	//impelemention by DynamicBuilder
	public default Object get(Request req, PathMatcher matcher) {
		return new IllegalArgumentException("method get none implemention");
	}
	
	public default Object post(Request req, PathMatcher matcher) {
	    return new IllegalArgumentException("method post none implemention");
	}
	
	public default Object put(Request req, PathMatcher matcher) {
	    return new IllegalArgumentException("method put none implemention");
	}
	
	public default Object delete(Request req, PathMatcher matcher) {
	    return new IllegalArgumentException("method delete none implemention ");
	}
	
	public default Object options(Request req, PathMatcher matcher) {
	    return new IllegalArgumentException("method options none implemention");
	}

}
