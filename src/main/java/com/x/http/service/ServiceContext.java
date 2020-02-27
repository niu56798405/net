package com.x.http.service;

import java.lang.reflect.Modifier;
import java.util.List;

import com.x.http.service.uri.PathMap;
import com.x.http.service.uri.PathMatcher;
import com.x.http.service.uri.PathPattern;
import com.x.http.service.uri.PathTemplate;
import com.x.injection.ApplicationContext;
import com.x.injection.Bean;
import com.x.injection.Inject;
import com.x.injection.Injection;
import com.x.injection.ApplicationContext.Finalizer;

/**
 * @author 
 */
@Bean
public class ServiceContext implements Finalizer {
    
    @Inject
    private ServiceConfiguration configuration;
    
    private PathMap<Pair> services;
    
    private ServiceConflictHandler conflictHandler;
    
    public ServiceContext() {
        services = new PathMap<>();
        conflictHandler = this::conflict;
    }
    
    public void registService(String path, Service service) {
    	registService(path, service, conflictHandler);
    }
    
	public void registService(String path, Service service, ServiceConflictHandler conflictHandler) {
    	PathTemplate temp = new PathTemplate(path);
    	PathPattern pattern = new PathPattern(temp);
    	Pair old = services.put(temp.mapping(), new Pair(pattern, service));
    	if(old != null) conflictHandler.handle(path, old.serivce, service);
    }
    
    public static class ServiceInvoker {
    	final PathMatcher matcher;
    	final Service service;
    	ServiceInvoker(PathMatcher matcher, Service service) {
    		this.matcher = matcher;
    		this.service = service;
		}
    	public Response invoke(Request req) {
    		return service.service(req, matcher);
    	}
    }
    
    public static class Pair {
    	PathPattern pattern;
    	Service serivce;
    	Pair(PathPattern pattern, Service service) {
    		this.pattern = pattern;
    		this.serivce = service;
		}
    }

    public ServiceInvoker get(String path) {
    	Pair pair = services.get(path);
    	if(pair != null) {
    		PathMatcher matcher = pair.pattern.compile(path);
    		if(matcher.find()) {
    			return new ServiceInvoker(matcher, pair.serivce);
    		}
    	}
    	return null;
    }

    public int size() {
        return services.size();
    }
    
    public FileHandler fileHandler() {
        return configuration.getFileHandler();
    }
    public ServiceErrorHandler errorHandler() {
        return configuration.getErrorhandler();
    }
    public RequestInteceptor Interceptor() {
        return configuration.getInteceptor();
    }
    
    @Override
    public void finalize(List<Class<?>> clazzes) {
        registServices(clazzes, this::conflict);
    }
    
    private void conflict(String path, Service s1, Service s2) {
		throw new IllegalArgumentException(String.format("conflict path %s in [%s:%s]", path, s1, s2));
	}

	public void registServices(List<Class<?>> clazzes, ServiceConflictHandler conflictHandler) {
		for (Class<?> clazz : clazzes) registService(clazz, conflictHandler);
	}
	
	public void registService(Class<?> clazz) {
		registService(clazz, this::conflict);
	}
    public void registService(Class<?> clazz, ServiceConflictHandler conflictHandler) {
        Http ann = clazz.getAnnotation(Http.class);
        if(ann != null && !Modifier.isAbstract(clazz.getModifiers()) && !Modifier.isInterface(clazz.getModifiers())) {
            ServiceConflictHandler osch = this.conflictHandler;
            this.conflictHandler = conflictHandler;
            registService(ann.value(), (Service) Injection.inject(newInstance(clazz)), conflictHandler);
            this.conflictHandler = osch;
        }
    }

    private Service newInstance(Class<?> clazz) {
        try {
            ServiceBuilder builder = ApplicationContext.fetchBean(ServiceBuilder.class);
            return  (builder == null ? (Service) clazz.newInstance() : builder.build(clazz));
        } catch (Exception e) {
            throw new IllegalArgumentException(clazz.getName(), e);
        }
    }

}
