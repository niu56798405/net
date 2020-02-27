package com.x.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.x.injection.ApplicationContext;

public class Monitor {
	
	static final Logger logger = LoggerFactory.getLogger(Monitor.class);
	
	final Class<?> ident;
	
	final AtomicLong cnt = new AtomicLong();//执行次数
	final AtomicLong sum = new AtomicLong();//执行总时间
	final AtomicLong max = new AtomicLong();//最长执行时间
	final AtomicLong wat = new AtomicLong();//最长等待时间
	
	public Monitor(Class<?> ident) {
		this.ident = ident;
	}
	
	static final Map<Class<?>, Monitor> monitors = new HashMap<>();
	
	static Monitor get(Class<?> ident) {
		Monitor monitor = monitors.get(ident);
		if(monitor == null) {
			synchronized (monitors) {
				monitor = monitors.get(ident);
				if(monitor == null) {
					monitor = new Monitor(ident);
					monitors.put(ident, monitor);
				}
			}
		}
		return monitor;
	}
	
	static void updateMax(AtomicLong s, long max) {
		for(;;) {
			long current = s.get();
			if(max < current) 
				break;
			if(s.compareAndSet(current, max))
				break;
		}
	}
	
	@Override
	public String toString() {
		return "" +
			   cnt.get() + '\t' +
			   sum.get() + '\t' +
			   sum.get() / cnt.get() + '\t' +
			   max.get() + '\t' +
			   wat.get() + '\t' +
			   ident.getName();
	}
	
	public static Map<Class<?>, Monitor> monitors() {
	    return monitors;
	}
	
	public static String columns() {
		return  "count\t" + 
				"sum  \t" +
				"avg  \t" +
				"max  \t" +
			    "wait \t" +
			    "ident\t";
	}
	
	public static void log(Class<?> ident, long create, long start, long end, Sequential sequential) {
		if(ApplicationContext.isInitializing()) return;//初始化不记录log
		
		long used = end - start;
		long wait = start - create;

		log(ident, used, wait);

		if (used > 1000 || wait > 6000) {
			logger.warn("Execute slow [" + ident.getName() + "] used: " + used + ", wait: " + wait + ", size: " + sequential.waitings());
		}
	}
	
	public static void log(Class<?> ident, long create, long start, long end) {
		log(ident, end - start, start - create);
	}

	public static void log(Class<?> ident, long used, long wait) {
		try {
			Monitor monitor = get(ident);
			
			monitor.cnt.incrementAndGet();
			monitor.sum.addAndGet(used);
			
			updateMax(monitor.max, used);
			updateMax(monitor.wat, wait);
		} catch (Throwable e) {
			//ignore
		}
	}
	
	public interface Sequential {
		int waitings();
	}

}
