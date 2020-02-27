package com.x.modular.code;

import com.x.injection.Prototype;
import com.x.injection.code.PrototypePatcher;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtNewConstructor;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;

/**
 * 
 * Generate module proxy add inject code
 * 
 * @author 
 *
 */
public class GenMP {
    
    public static Class<?> generate(Class<?> clazz) {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctParent = pool.getCtClass(clazz.getName());

            CtClass ct = pool.makeClass(clazz.getName() + "$$Proxy");
            ct.setSuperclass(ctParent);
            
            ct.addConstructor(CtNewConstructor.defaultConstructor(ct));
            
            ClassFile ccfile = ct.getClassFile();
            ConstPool ccpool = ccfile.getConstPool();
            
            AnnotationsAttribute attr = new AnnotationsAttribute(ccpool, AnnotationsAttribute.visibleTag);
            attr.addAnnotation(new Annotation(ccpool, pool.get(Prototype.class.getName())));
            ccfile.addAttribute(attr);

            new PrototypePatcher().patch(ct);
            
            return ct.toClass();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

}
