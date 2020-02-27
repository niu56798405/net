package com.x.injection.code;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.Modifier;
import javassist.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CodePatcher {
    
    static final Logger logger = LoggerFactory.getLogger(CodePatcher.class);
    
    static final List<Patcher> patchers = new ArrayList<Patcher>();
    static {
        patchers.add(new JavaBeanPatcher());
        patchers.add(new PrototypePatcher());
    }

    public static void makePatch(List<String> classNames) {
        ClassPool pool = ClassPool.getDefault();
        Set<CtClass> patchedClasses = new HashSet<CtClass>();
        for (String className : classNames) {
            try {
                makePatch(pool, patchedClasses, pool.get(className));
            } catch (NotFoundException | CannotCompileException | ClassNotFoundException e) {
                logger.error("Patch code  error", e);
            }
        }
    }

    private static void makePatch(ClassPool pool, Set<CtClass> patchedClasses, CtClass clazz) throws NotFoundException, CannotCompileException, ClassNotFoundException {
        if(patchedClasses.contains(clazz) || dispensablePatch(clazz)) return;
        
        makePatch(pool, patchedClasses, clazz.getSuperclass());//先处理父类

        boolean patched = false;
        
        for(Patcher patcher : patchers) {
            if(patcher.required(clazz)) {
                patcher.patch(clazz);
                patched = true;
            }
        }

        if(patched) clazz.toClass();
        
        patchedClasses.add(clazz);
    }

    private static boolean dispensablePatch(CtClass clazz) throws NotFoundException {
        return "java.lang.Object".equals(clazz.getName()) || Modifier.isInterface(clazz.getModifiers());
    }

}
