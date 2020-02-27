package com.x.injection.code;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;

public interface Patcher {
    
    public boolean required(CtClass clazz) throws NotFoundException, ClassNotFoundException;
    
    public void patch(CtClass clazz) throws CannotCompileException, NotFoundException, ClassNotFoundException;

}
