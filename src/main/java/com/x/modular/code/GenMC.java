package com.x.modular.code;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.x.game.player.Player;
import com.x.injection.code.CombineBuilder;
import com.x.modular.Modular;
import com.x.modular.ModularInject;
import com.x.modular.ModularLoad;
import com.x.modular.ModularSave;
import com.x.modular.ModularType;
import com.x.modular.ModularUnload;
import com.x.modular.Module;
import com.x.modular.code.ModularAnalyzer.ModularInfo;
import com.x.tools.Bubble;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.NotFoundException;

/**
 * 
 * 生成ModuleContainer相关代码
 * 
 * addFiled _madules
 * addMethod _initModules
 * addMethod _loadModule(index)
 * addConstructor(and exec _initModules)
 * @author 
 * 
 */
public class GenMC {
    
    
    static final String MODULES_CONTAINER_SUFIX = "$$Proxy";
    static final String MODULES_FIELD_NAME = "_modules";
    static final String MODULES_INIT_METHOD = "_initModules";
    static final String MODULES_LOAD_METHOD = "_loadModule";//by index
    static final String LOGGER_FIELD_NAME = "_logger";
    
    /**
     * addFiled MODULES_FIELD_NAME
     * addMethod MODULES_INIT_METHOD
     * addMethod MODULES_LOAD_METHOD(index)
     * addConstructor(for execute MODULES_INIT_METHOD)
     * @param pool 
     * @param mcClazz  有且仅有一个构造函数 Factory的返回类型
     * @param indexes
     * @throws Exception 
     */
    public static String generate(ClassPool pool, Class<? extends Player> mcClazz, List<ModularInfo> infos) throws Exception {
        //分析需要注入的属性(public module.share();)
        Map<Class<?>, Field> injectFields = analyzeInjectFields(mcClazz);
        
        CtClass cc = makeClassInitialize(pool, mcClazz, infos, injectFields);
        
        makeLoadMethod(cc, infos, injectFields);

        makeUnloadMethod(cc, infos, injectFields);

        makeSaveMethod(cc, infos);

        makeLoadModuleMethod(cc);
        
        return cc.toClass().getName();
    }
    
    private static void makeSaveMethod(CtClass cc, List<ModularInfo> infos) throws CannotCompileException {
        //make method save
        StringBuilder tmp = new StringBuilder("public boolean save() {");
        for (ModularInfo info : infos) {
            Class<?> clazz = info.clazz;
            String clazzName = clazz.getName();
            String clazzTmpName = clazzName.replaceAll("\\.", "_") + "_tmp";
            
            tmp.append(clazzName).append(" ").append(clazzTmpName).append("=(").append(clazzName).append(")this.").append(MODULES_FIELD_NAME).append("[").append(info.index).append("];")
                .append("if(").append(clazzTmpName).append(" != null) {")
                .append("try {");
            //add save methods
            for (Method method : findMethodsByAnno(clazz, ModularSave.class)) {
                    tmp.append(clazzTmpName).append(".").append(method.getName()).append("(").append(String.join(",", method.getAnnotation(ModularSave.class).value())).append(");");
            }
            tmp.append("} catch (Throwable t) {")
                .append(LOGGER_FIELD_NAME).append(".warn(\"save module error:\", t);")
                .append("}");
            tmp.append("}");
        }
        tmp.append("return super.save();")
            .append("}");
        cc.addMethod(CtNewMethod.make(tmp.toString(), cc));
    }
    
    private static void makeUnloadMethod(CtClass cc, List<ModularInfo> infos, Map<Class<?>, Field> injectFields) throws Exception {
        //make method unload
        StringBuilder unloadResident = new StringBuilder();
        StringBuilder unloadTransient = new StringBuilder();
        for (ModularInfo info : infos) {
            Class<?> clazz = info.clazz;
            String clazzName = clazz.getName();
            String clazzTmpName = clazzName.replaceAll("\\.", "_") + "_tmp";
            
            StringBuilder tmp = (clazz.getAnnotation(Modular.class).value() == ModularType.RESIDENT ? unloadResident : unloadTransient);
            tmp.append(clazzName).append(" ").append(clazzTmpName).append("=(").append(clazzName).append(") this.").append(MODULES_FIELD_NAME).append("[").append(info.index).append("];")
               .append("if("+clazzTmpName+" != null){");
            //add unload methods
            for (Method method : findMethodsByAnno(clazz, ModularUnload.class)) {
                tmp.append(clazzTmpName).append(".").append(method.getName()).append("(").append(String.join(",", method.getAnnotation(ModularUnload.class).value())).append(");");
            }
            
            tmp.append("} else {").append(LOGGER_FIELD_NAME).append(".error(\"").append(clazzName).append(" is NULL\");}");
            //删除Field
            tmp.append(makeUnload4InjectField(clazz, clazzTmpName, injectFields));
            
            tmp.append("this.").append(MODULES_FIELD_NAME).append("[").append(info.index).append("] = null;");
        }
        StringBuilder unload = new StringBuilder()
        .append("public synchronized boolean unload(").append(ModularType.class.getName()).append(" type) {")
        .append("try {")
            .append("if(").append(ModularType.class.getName()).append(".").append(ModularType.RESIDENT.name()).append(" == $1){")
                .append(unloadResident)
            .append("} else {")
                .append(unloadTransient)
            .append("}")
        .append("} catch (Throwable t) {")
            .append(LOGGER_FIELD_NAME).append(".error(\"unload module error:\", t);")
            .append("return false;")
        .append("}")
        .append("return super.unload($1);")
        .append("}");
        cc.addMethod(CtNewMethod.make(unload.toString(), cc));
    }
    
    private static String makeUnload4InjectField(Class<?> clazz, String clazzTmpName, Map<Class<?>, Field> injectFields) throws Exception {
        StringBuilder ret = new StringBuilder();
        Class<?>[] interfazes = clazz.getInterfaces();
        for (Class<?> interfaze : interfazes) {
            Field injectField = injectFields.get(interfaze);
            if(injectField != null && injectField.getAnnotation(ModularInject.class).combine()) {
                ret.append("((").append(CombineBuilder.buildClass(injectField.getType()).getName()).append(")").append(injectField.getName()).append(").").append(CombineBuilder.REMOVE_COMBINE_DELEGATE_METHOD).append("(").append(clazzTmpName).append(");");
            }
        }
        
        Field injectField = injectFields.get(clazz.getMethod("share").getReturnType());
        if(injectField != null)
            ret.append(injectField.getName()).append("=null;").toString();
        return ret.toString();
    }
    
    private static void makeLoadModuleMethod(CtClass cc) throws CannotCompileException {
        //make method MODULES_LOAD_METHOD(index)
        StringBuilder load = new StringBuilder()
        .append("public ").append(Module.class.getName()).append(" ").append(MODULES_LOAD_METHOD).append("(int index) {")
        .append("return this.").append(MODULES_FIELD_NAME).append("[$1];")
        .append("}");
        cc.addMethod(CtNewMethod.make(load.toString(), cc));
    }
    
    private static void makeLoadMethod(CtClass cc, List<ModularInfo> infos, Map<Class<?>, Field> injectFields) throws Exception {
        //make method load
        StringBuilder loadResident = new StringBuilder();
        StringBuilder loadTransient = new StringBuilder();
        for (ModularInfo info : infos) {
            Class<?> clazz = info.proxyClazz = GenMP.generate(info.clazz);
            String clazzName = clazz.getName();
            String clazzTmpName = clazzName.replaceAll("\\.", "_") + "_tmp";
            
            StringBuilder tmp = (clazz.getAnnotation(Modular.class).value() == ModularType.RESIDENT ? loadResident : loadTransient)
                .append("if(this.").append(MODULES_FIELD_NAME).append("[").append(info.index).append("]==null) {")
                //new
                .append(clazzName).append(" ").append(clazzTmpName).append("= new ").append(clazzName).append("();")
                //set to modules field
                .append("this.").append(MODULES_FIELD_NAME).append("[").append(info.index).append("]=").append(clazzTmpName).append(";");
            
            //add load methods
            List<Method> methods = Bubble.sort(findMethodsByAnno(clazz, ModularLoad.class),
                    (Method m1, Method m2)->Integer.compare(m1.getAnnotation(ModularLoad.class).order(), m2.getAnnotation(ModularLoad.class).order()));
            for (Method method : methods) {
                tmp.append(clazzTmpName).append(".").append(method.getName()).append("(").append(String.join(",", method.getAnnotation(ModularLoad.class).value())).append(");");
            }
            
            //注入Field
            tmp.append(makeLoad4InjectField(info.clazz, clazzTmpName, injectFields))
            .append("}");
        }
        StringBuilder load = new StringBuilder()
            .append("public synchronized boolean load(").append(ModularType.class.getName()).append(" type) {")
            .append("try {")
                .append("if(").append(ModularType.class.getName()).append(".").append(ModularType.RESIDENT.name()).append(" == $1) {")
                    .append(loadResident)
                .append("} else {")
                    .append(loadTransient)
                .append("}")
            .append("} catch (Throwable t) {")
                .append(LOGGER_FIELD_NAME).append(".error(\"load module error:\", t);")
                .append("return false;")
            .append("}")
            .append("return super.load($1);")
            .append("}");

        cc.addMethod(CtNewMethod.make(load.toString(), cc));        
    }
    
    private static String makeLoad4InjectField(Class<?> clazz, String clazzTmpName, Map<Class<?>, Field> injectFields) throws Exception {
        StringBuilder ret = new StringBuilder();
        
        Class<?>[] interfazes = clazz.getInterfaces();
        for (Class<?> interfaze : interfazes) {
            Field injectField = injectFields.get(interfaze);
            if(injectField != null && injectField.getAnnotation(ModularInject.class).combine()) {
                ret.append("((").append(CombineBuilder.buildClass(injectField.getType()).getName()).append(")").append(injectField.getName()).append(").").append(CombineBuilder.APPEND_COMBINE_DELEGATE_METHOD).append("(").append(clazzTmpName).append(");");
            }
        }
        
        Field injectField = injectFields.get(clazz.getMethod("share").getReturnType());
        if(injectField != null) {
            ret.append(injectField.getName()).append("=").append(clazzTmpName).append(".share();");
        }
        return ret.toString();
    }

    private static CtClass makeClassInitialize(ClassPool pool, Class<? extends Player> mcClazz, List<ModularInfo> infos,  Map<Class<?>, Field> injectFields) throws NotFoundException, CannotCompileException {
        CtClass ctParent = pool.get(mcClazz.getName());
        //make class
        CtClass cc = pool.makeClass(mcClazz.getName() + MODULES_CONTAINER_SUFIX);
        cc.setSuperclass(ctParent);

        //make field __modules__
        CtField cf = CtField.make(String.format("private %s[] %s;", Module.class.getName(), MODULES_FIELD_NAME), cc);
        cc.addField(cf);
        
        CtField lgcf = CtField.make("private static org.slf4j.Logger " + LOGGER_FIELD_NAME + " = org.slf4j.LoggerFactory.getLogger(" + mcClazz.getName() + MODULES_CONTAINER_SUFIX + ".class);", cc);
        cc.addField(lgcf);
        
        //make init method
        StringBuilder initMethod = new StringBuilder()
            .append("private void ").append(MODULES_INIT_METHOD).append("() {")
            .append("this.").append(MODULES_FIELD_NAME).append(" = new ").append(Module.class.getName()).append("[").append(infos.size()).append("];")
            .append(makeInitialize4InjectFields(injectFields))
            .append("}");
        cc.addMethod(CtNewMethod.make(initMethod.toString(), cc));

        //make constructor
        CtConstructor con = ctParent.getConstructors()[0];
        StringBuilder params = new StringBuilder();
        for (int i = 1; i <= con.getParameterTypes().length; i++) {
            params.append((i == 1) ? "$" : ",$").append((i));
        }
        
        CtConstructor ccon = CtNewConstructor.copy(con, cc, null);
        StringBuilder cconBody = new StringBuilder()
            .append("{")
            .append("super(").append(params).append(");")
            .append("this.").append(MODULES_INIT_METHOD).append("();")
            .append("}");
        ccon.setBody(cconBody.toString());
        cc.addConstructor(ccon);
        return cc;
    }

    private static String makeInitialize4InjectFields(Map<Class<?>, Field> injectFields) {
        StringBuilder ret = new StringBuilder();
        for (Field injectField : injectFields.values()) {
            if(injectField.getAnnotation(ModularInject.class).combine()) {
                Class<?> clazz = CombineBuilder.buildClass(injectField.getType());
                ret.append(injectField.getName()).append("=new ").append(clazz.getName()).append("();");
            }
        }
        return ret.toString();
    }

    private static Map<Class<?>, Field> analyzeInjectFields(Class<? extends Player> mcClazz) {
        Map<Class<?>, Field> injectFields = new HashMap<Class<?>, Field>();
        Field[] fields = mcClazz.getFields();
        for (Field field : fields) {
            if(field.getAnnotation(ModularInject.class) != null) {
                Class<?> type = field.getType();
                if(!type.isInterface()) {
                    throw new IllegalArgumentException("inject field must be an interface : " + type);
                }
                injectFields.put(type, field);
            }
        }
        return injectFields;
    }
    
    private static List<Method> findMethodsByAnno(Class<?> clazz, Class<? extends Annotation> anno) {
        return Bubble.sort(findMethodsByAnno0(clazz, anno, new ArrayList<>()), (Method o1, Method o2) -> o1.getName().compareTo(o2.getName()));
    }

    private static List<Method> findMethodsByAnno0(Class<?> clazz, Class<? extends Annotation> anno, List<Method> mets) {
        if(!clazz.equals(Object.class)) {
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if(method.isAnnotationPresent(anno)) {
                    mets.add(method);
                }
            }
            findMethodsByAnno0(clazz.getSuperclass(), anno, mets);
        }
        return mets;
    }
    
}
