package com.x.http.service;

import com.x.http.service.uri.PathMatcher;

/**
 * 
 * http service method interface
 * @author 
 * 
 */
public interface Service {
	
	public default Response service(Request req, PathMatcher matcher) {
		return service(req);
	}
    
    /**
     * @param req
     * @return resp (just support String)
     */
    public Response service(Request req);

}
