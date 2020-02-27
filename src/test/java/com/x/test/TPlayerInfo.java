package com.x.test;

import java.lang.reflect.Method;

import org.junit.Ignore;

import com.x.injection.code.JavaBean;
@Ignore
@JavaBean
public class TPlayerInfo extends TInfo {
    
    private String name;

    public TPlayerInfo(int id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public TPlayerInfo() {
    }
    
    public String rename(String name) {
        String old = this.name;
        this.name = name;
        return old;
    }
    
    public void println() {
        String mnames = "TPlayerInfo[";
        for (Method method : TPlayerInfo.class.getDeclaredMethods()) {
            mnames += method.getName() + ",";
        }
        mnames += "], TInfo[";
        for (Method method : TInfo.class.getDeclaredMethods()) {
            mnames += method.getName() + ",";
        }
        mnames += "]";
        System.out.println(mnames);
    }

}
