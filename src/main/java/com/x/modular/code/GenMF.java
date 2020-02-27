package com.x.modular.code;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;

import com.x.modular.ModularEnigne;

/**
 * 
 * Generate ModuleContainerFactory by given interface
 * 
 * @author 
 *
 */
public class GenMF {

    @SuppressWarnings("unchecked")
    public static <T> T generate(Class<T> mcFactoryInterface) {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctParent = pool.getCtClass(mcFactoryInterface.getName());

            CtClass ct = pool.makeClass(mcFactoryInterface.getName() + "$$Proxy");
            ct.addInterface(ctParent);

            CtMethod[] methods = ctParent.getDeclaredMethods();
            assert methods.length == 1;
            CtMethod pmethod = methods[0];
            
            CtMethod method = CtNewMethod.copy(pmethod, ct, null);
            CtClass[] params = pmethod.getParameterTypes();
            StringBuilder body = new StringBuilder("{return new ").append(ModularEnigne.fetchMCClassName()).append("(");
            for (int i = 0; i < params.length; i++) {
                if(i > 0) body.append(",");
                body.append("$").append(i+1);
            }
            body.append(");}");
            method.setBody(body.toString());
            ct.addMethod(method);

            return (T) (ct.toClass().newInstance());
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
    
}
