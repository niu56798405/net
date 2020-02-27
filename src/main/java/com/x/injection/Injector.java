package com.x.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.x.injection.code.ProxyBuilder;
import com.x.injection.code.ProxyBuilder.Supplier;
import com.x.tools.Strings;

public class Injector {
    
    public final Class<?> master;
    public final InjectField[] fields;
    public int index;
    public Injector(Class<?> master, InjectField[] fields) {
        this.master = master;
        this.fields = fields;
    }
    public <T> T inject(T bean) {
        try {
            for (InjectField field : fields) {
                field.inject(bean);
            }
            return bean;
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    public static Injector build(Class<?> clazz) {
        return build(clazz, null);
    }
    
    @Override
    public String toString() {
        return master.toString();
    }
    /**
     * @param clazz
     * @param upwardsAnno 如果父类也有标注对应的Annotation则不处理父类
     * @return
     */
    public static Injector build(Class<?> clazz, Class<? extends Annotation> upwardsAnno) {
        List<InjectField> injectFields = new ArrayList<InjectField>();
        Class<?> t = clazz;
        do {
            Field[] fields = t.getDeclaredFields();
            for (Field field : fields) {
                Inject anno = field.getAnnotation(Inject.class);
                if(anno != null) {
                    InjectField injectField = Strings.isEmpty(anno.value()) ? new InjectField(field) : new NameInjectField(field);
                    injectFields.add(injectField);
                }
            }
            t = t.getSuperclass();
        } while((upwardsAnno == null || !t.isAnnotationPresent(upwardsAnno)) && !Object.class.equals(t));
        return new Injector(clazz, injectFields.toArray(new InjectField[0]));
    }
    
    public static class InjectField implements Supplier {
        public final Field field;
        public final boolean isLazy;
        public final boolean nullable;
        public InjectField(Field field) {
            this.field = field;
            this.field.setAccessible(true);
            this.isLazy = field.isAnnotationPresent(Inject.class) && field.getAnnotation(Inject.class).lazy();
            this.nullable = field.isAnnotationPresent(Inject.class) && field.getAnnotation(Inject.class).nullable();
        }
        public Class<?> type() {
            return field.getType();
        }
        private Object cache;
        public final void inject(Object bean) throws IllegalArgumentException, IllegalAccessException {
            if(cache == null && (cache = fetch()) == null)//优化偿试获取bean, 没有时build lazy proxy
                cache = ProxyBuilder.canProxy(type()) ? ProxyBuilder.build(type(), this) : get();
            
            field.set(bean, cache);
        }
        @Override
        public final Object get() {
            Object bean = fetch();
            if(!nullable && bean == null)
                throw new IllegalArgumentException(this.field.getType().getName() + " doesn`t loaded");
            return bean;
        }
        protected Object fetch() {
            return ApplicationContext.fetchBean(field.getType());
        }
        @Override
        public String toString() {
            return field.getType().getName() + " " + field.getName() + (isLazy?" lazy":"");
        }
    }
    
    public static class NameInjectField extends InjectField {
        public final String name;
        public NameInjectField(Field field) {
            super(field);
            this.name = field.getAnnotation(Inject.class).value();
        }
        @Override
        protected Object fetch() {
            return ApplicationContext.fetchBean(name);
        }
        @Override
        public String toString() {
            return field.getType().getName() + "@" + this.name + " " + field.getName() + (isLazy?" lazy":"");
        }
    }
    
}