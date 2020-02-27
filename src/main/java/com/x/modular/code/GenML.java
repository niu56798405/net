package com.x.modular.code;

import java.util.HashMap;
import java.util.Map;

import com.x.modular.ModuleLoader;
import com.x.modular.ModuleTypeLoader;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;

/**
 * 
 * Module Loader gen
 * @author 
 *
 */
public class GenML {
    
    public static Map<Class<?>, ModuleTypeLoader> generate(ClassPool pool, String mcClazzName, Map<Class<?>, Integer> indexes) throws Exception {
        Map<Class<?>, ModuleTypeLoader> ret = new HashMap<Class<?>, ModuleTypeLoader>();
        
        for (Class<?> key : indexes.keySet()) {
            CtClass ctParent = pool.getCtClass(ModuleTypeLoader.class.getName());

            int index = indexes.get(key);
            
            CtClass ct = pool.makeClass(ModuleTypeLoader.class.getName() + "$$Proxy" + index);
            ct.addInterface(ctParent);

            CtMethod[] methods = ctParent.getDeclaredMethods();
            assert methods.length == 1;
            
            //make loadModule method
            CtMethod method = CtNewMethod.copy(methods[0], ct, null);
            StringBuilder body = new StringBuilder("{")
                .append("return ((").append(mcClazzName).append(") $1).").append(GenMC.MODULES_LOAD_METHOD).append("(").append(index).append(");")
                .append("}");
            method.setBody(body.toString());
            ct.addMethod(method);
            
            ret.put(key, (ModuleTypeLoader) ct.toClass().newInstance());
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    public static ModuleLoader generate(String mccn, Map<Class<?>, ModuleTypeLoader> loaders) {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctParent = pool.getCtClass(ModuleLoader.class.getName());

            CtClass ct = pool.makeClass(ModuleLoader.class.getName() + "$$Proxy");
            ct.addInterface(ctParent);

            CtField ctField = CtField.make("private java.util.Map loaders;", ct);
            ct.addField(ctField);
            
            CtClass[] paramters = new CtClass[]{pool.get(Map.class.getName())};
            CtConstructor ctConstructor = new CtConstructor(paramters, ct);
            ctConstructor.setBody("{this.loaders = $1;}");
            ct.addConstructor(ctConstructor);
            
            CtMethod[] methods = ctParent.getDeclaredMethods();
            assert methods.length == 1;
            
            CtMethod method = CtNewMethod.copy(methods[0], ct, null);
            StringBuilder body = new StringBuilder("{")
            .append("if($1 instanceof ").append(mccn).append(") {")
                .append(ModuleTypeLoader.class.getName()).append(" _tmp = ").append("(").append(ModuleTypeLoader.class.getName()).append(") this.loaders.get($2);")
                .append("return _tmp.load($1);")
            .append("} ")
                .append("return null;")
            .append("}");
            method.setBody(body.toString());
            ct.addMethod(method);
            
            return (ModuleLoader) (ct.toClass().getConstructor(Map.class).newInstance(loaders));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
    
}
