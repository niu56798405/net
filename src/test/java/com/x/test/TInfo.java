package com.x.test;

import com.x.injection.code.JavaBean;

@JavaBean
public class TInfo {

    protected int id;
    
    public TInfo() {
    }
    
    public TInfo(int id) {
    	this.id = id;
    }
    
    public int getId() {
        return this.id;
    }
    
}
