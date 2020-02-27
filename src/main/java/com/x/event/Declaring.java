package com.x.event;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.x.tools.ReflectionUtil;


/**
 * 
 * listeners with @EventListener declared by class 
 * @author luzj
 *
 */
public final class Declaring {
    
    final int group;
    final int type;
    final Method method;
    
    public Declaring(int group, Method m) {
        this.group = group;
        this.type = m.getAnnotation(EventListener.class).value();
        this.method = m;
        this.method.setAccessible(true);
    }
    
    static AtomicInteger groupGenerator = new AtomicInteger();
    static Map<Class<?>, Integer> groups = new HashMap<>();
    public static int getGroup(Class<?> declared) {
        Integer group = groups.get(declared);
        return group == null ? getGroupSafely(declared) : group.intValue();
    }
    public static int getGroup() {
        return getGroup(ReflectionUtil.getCallerClass());
    }
    
    private static synchronized int getGroupSafely(Class<?> declared) {
        Integer group = groups.get(declared);
        if(group != null){
            return group.intValue();
        }
        
        int g = groupGenerator.incrementAndGet();
        groups.put(declared, g);
        return g;
    }

    public static void regist(Object declaredObj, Registrator registrator) {
        regist(declaredObj.getClass(), declaredObj, registrator);
    }
    public static void regist(Class<?> declared, Object declaredObj, Registrator registrator) {
        Declaring[] declaring = get(declared);
        for (Declaring r : declaring) {
            registrator.regist(new DeclaringListener(r.group, r.type, declaredObj, r.method));
        }
    }
    
    static Map<Class<?>, Declaring[]> registrators = new HashMap<>();
    private static Declaring[] get(Class<?> declared) {
        Declaring[] r = registrators.get(declared);
        return r == null ? getSafely(declared) : r;
    }

    private static synchronized Declaring[] getSafely(Class<?> declared) {
        Declaring[] r = registrators.get(declared);
        if(r == null) {
            r = build(declared);
            registrators.put(declared, r);
        }
        return r;
    }
    
    public static void unregist(Class<?> declared, Registrator registrator) {
        registrator.unregist(getGroup(declared));
    }

    private static Declaring[] build(Class<?> declared) {
        int group = getGroup(declared);
        return Arrays.stream(declared.getMethods())
                .filter(m -> m.isAnnotationPresent(EventListener.class) && Event.class.isAssignableFrom(getOnlyParamType(m)))
                .map(m -> new Declaring(group, m))
                .toArray(s -> new Declaring[s]);
    }

    static class DeclaringListener extends Listener<Event> {
    	final Object declareObj;
    	final Method method;
    	public DeclaringListener(int group, int type, Object declare, Method method) {
    		super(group, type);
    		this.declareObj = declare;
    		this.method = method;
    	}
		@Override
		public void onEvent(Event event) throws Exception {
			method.invoke(declareObj, event);
		}
    }
    
    private static Class<?> getOnlyParamType(Method method) {
        Class<?>[] types = method.getParameterTypes();
        return types.length == 1 ? types[0] : Void.class;
    }

}
