package com.x.http.service.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.x.http.service.Http;
import com.x.http.service.Request;
import com.x.http.service.Service;
import com.x.http.service.ServiceBuilder;
import com.x.http.service.ServiceContext;
import com.x.injection.Bean;
import com.x.injection.Inject;
import com.x.injection.Injection;
import com.x.tools.Strings;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.bytecode.LocalVariableAttribute;

@Bean
public class RestServiceBuilder implements ServiceBuilder {
	
	@Inject
	private RestConfiguration configuration;
	@Inject(lazy=true)
	private ServiceContext serviceContext;
	
    public void setConfiguration(RestConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setServiceContext(ServiceContext serviceContext) {
		this.serviceContext = serviceContext;
	}

	@Override
    public Service build(Class<?> clazz) throws Exception {
        if(RestService.class.isAssignableFrom(clazz)) {
            Service dynamic = buildDynamic(clazz);
            if(dynamic != null) return dynamic;
        }
        return (Service) clazz.newInstance();
    }

	public Service buildDynamic(Class<?> clazz) {
		try {
			CtMethod[] cms = findMethods(clazz, false);
			if(cms.length == 0) return null;
			RestService service = (RestService) Injection.makeInstanceAndInject(clazz);
			buildSubResourceService(clazz, service);
			return buildAdapter(buildDynamicService(service, cms));
		} catch (Throwable e) {
			//ignore
			throw new IllegalArgumentException(e);
		}
	}

	private void buildSubResourceService(Class<?> clazz, RestService service) throws Exception {
		AtomicInteger index = new AtomicInteger(0);
        Arrays.stream(findMethods(clazz, true)).collect(Collectors.groupingBy(cm->findSubresPath(clazz, cm))).forEach((k, v) -> {
            //check repeated http methods
            serviceContext.registService(k, buildAdapter(buildDynamicService(service, v.toArray(new CtMethod[0]), "_res" + index.incrementAndGet())));
        });
	}

    private String findSubresPath(Class<?> clazz, CtMethod cm) {
        try {
            return Strings.trim(clazz.getAnnotation(Http.class).value(), '/') + '/' + Strings.trim(findMethodResPath(clazz, cm), '/');
        } catch (Throwable e) {
            throw new IllegalArgumentException(e);
        }
    }

	private ServiceAdapter buildAdapter(RestService service) {
		return new ServiceAdapter(service, configuration.getRespEncoder());
	}
	
	private RestService buildDynamicService(RestService origin, CtMethod[] httpMethods) throws Exception {
		return buildDynamicService(origin, httpMethods, "");
	}
	
	@SuppressWarnings("unchecked")
	private RestService buildDynamicService(RestService origin, CtMethod[] httpMethods, String suffix) {
		try {
            ClassPool pool = ClassPool.getDefault();
            CtClass p = pool.get(RestService.class.getName());
            Class<?> clazz = origin.getClass();
            CtClass c = pool.makeClass(clazz.getName()+"$$dynamic"+suffix);
            c.addInterface(p);
            
            c.addField(CtField.make(BodyDecoder.class.getName() + " _bodyDecoder;", c));
            c.addField(CtField.make(clazz.getName() + " _origin;", c));
            
            CtConstructor cc = CtNewConstructor.make(new CtClass[]{pool.get(BodyDecoder.class.getName()),pool.get(clazz.getName())}, new CtClass[0], c);
            cc.setBody("{this._bodyDecoder=$1;this._origin=$2;}");
            c.addConstructor(cc);
            
            for (CtMethod httpMethod : httpMethods) {
                CtMethod cm = p.getDeclaredMethod(findMethodName(httpMethod));
                StringBuilder body = new StringBuilder().append("{");
                StringBuilder argsStr = new StringBuilder();
                Argument[] args = findArgs(httpMethod);
                for (int i = 0; i < args.length; i++) {
                    Argument arg = args[i];
                    body.append(arg.type.getName()).append(" ").append("_").append(i).append(" = ").append(toDecodeStr(arg)).append(";");
                    argsStr.append(i == 0 ? "" : ",").append("_").append(i);
                }
                body.append("return this._origin.").append(httpMethod.getName()).append("(").append(argsStr).append(");").append("}");
                CtMethod m = CtNewMethod.copy(cm, c, null);
                m.setBody(body.toString());
                c.addMethod(m);
            }

            c.addMethod(CtNewMethod.make("public String toString() {return _origin.toString();}", c));
            
            return (RestService) c.toClass().getConstructor(BodyDecoder.class, clazz).newInstance(configuration.getBodyDecoder(), origin);
        } catch (Throwable e) {
            throw new IllegalArgumentException(e);
        }
	}
	
	private String findMethodName(CtMethod httpMethod) {
	    if(httpMethod.hasAnnotation(HttpMethods.POST.class)) {
	        return "post";
	    }
	    if(httpMethod.hasAnnotation(HttpMethods.PUT.class)) {
	        return "put";
	    }
	    if(httpMethod.hasAnnotation(HttpMethods.DELETE.class)) {
	        return "delete";
	    }
	    if(httpMethod.hasAnnotation(HttpMethods.GET.class)) {
	        return "get";
	    }
	    if(httpMethod.hasAnnotation(HttpMethods.OPTIONS.class)) {
	        return "options";
	    }
        throw new IllegalArgumentException("can`t be here");
    }

    public String toDecodeStr(Argument arg) {
		if(arg.origin == Origin.REQUEST) return "$1";
		if(arg.origin == Origin.QUERY) return Primitives.castStr(arg, "$1.getParam(\""+ arg.name+"\")");
		if(arg.origin == Origin.HEDAER) return Primitives.castStr(arg, "$1.getHeader(\""+ arg.name+"\")");
		if(arg.origin == Origin.BODY) return Primitives.castStr(arg, "_bodyDecoder.decode("+arg.type.getName()+".class,$1.content())");
		if(arg.origin == Origin.PATH) return Primitives.castStr(arg, "$2.group(\""+arg.name+"\")");
		throw new IllegalArgumentException("can`t be here");
	}

	private Argument[] findArgs(CtMethod cm) throws Exception {
		CtClass[] types = cm.getParameterTypes();
		Object[][] annoss = cm.getParameterAnnotations();
		Argument[] args = new Argument[types.length];
		for (int i = 0; i < args.length; i++) {
			Object[] annos = annoss[i];
			CtClass type = types[i];
			
			Origin origin = null;
			String value = null;
			
			if(type.getName().equals(Request.class.getName())) {
                origin = Origin.REQUEST;
                value = "";
            } else {
                for (Object anno : annos) {
                    if(anno instanceof RequestParam) {
                        origin = Origin.QUERY;
                        value = ((RequestParam) anno).value();
                        break;
                    } else if(anno instanceof RequestHeader) {
                        origin = Origin.HEDAER;
                        value = ((RequestHeader) anno).value();
                        break;
                    } else if(anno instanceof RequestPath) {
                        origin = Origin.PATH;
                        value = ((RequestPath) anno).value();
                        break;
                    } else if(anno instanceof RequestBody) {
                        origin = Origin.BODY;
                        break;
                    } 
                }
            }
			if("".equals(value))
			    value = ((LocalVariableAttribute)cm.getMethodInfo().getCodeAttribute().getAttribute(LocalVariableAttribute.tag)).variableName(i+1);
			
            if (origin == null)
                throw new IllegalArgumentException("not support argument type [" + type.getName() + ":" + value + "]");
            
			args[i] = new Argument(origin, type, value);
		}
		return args;
	}

	private CtMethod[] findMethods(Class<?> clazz, boolean subRes) throws Exception {
		CtClass cc = ClassPool.getDefault().get(clazz.getName());
		cc.defrost();
		CtMethod[] cms = cc.getDeclaredMethods();
		List<CtMethod> ms = new ArrayList<>();
		for (CtMethod cm : cms) {
		    String res = findMethodResPath(clazz, cm);
		    if(res != null && (Strings.isEmpty(res) != subRes)) {
		        ms.add(cm);
		    }
		}
		return ms.toArray(new CtMethod[0]);
	}
	
	private String findMethodResPath(Class<?> clazz, CtMethod cm) throws Exception {
	    if(cm.hasAnnotation(HttpMethods.GET.class)) {
            return cm.hasAnnotation(Http.class) ? ((Http) cm.getAnnotation(Http.class)).value() : ((HttpMethods.GET) cm.getAnnotation(HttpMethods.GET.class)).value();
	    }
	    if(cm.hasAnnotation(HttpMethods.POST.class)) {
	        return cm.hasAnnotation(Http.class) ? ((Http) cm.getAnnotation(Http.class)).value() : ((HttpMethods.POST) cm.getAnnotation(HttpMethods.POST.class)).value();
	    }
	    if(cm.hasAnnotation(HttpMethods.PUT.class)) {
	        return cm.hasAnnotation(Http.class) ? ((Http) cm.getAnnotation(Http.class)).value() : ((HttpMethods.PUT) cm.getAnnotation(HttpMethods.PUT.class)).value();
	    }
	    if(cm.hasAnnotation(HttpMethods.DELETE.class)) {
	        return cm.hasAnnotation(Http.class) ? ((Http) cm.getAnnotation(Http.class)).value() : ((HttpMethods.DELETE) cm.getAnnotation(HttpMethods.DELETE.class)).value();
	    }
	    if(cm.hasAnnotation(HttpMethods.OPTIONS.class)) {
	        return cm.hasAnnotation(Http.class) ? ((Http) cm.getAnnotation(Http.class)).value() : ((HttpMethods.OPTIONS) cm.getAnnotation(HttpMethods.OPTIONS.class)).value();
	    }
	    return null;
	}
	
	public static class Argument {
		public final Origin origin;
		public final CtClass type;
		public final String name;
		
		public Argument(Origin origin, CtClass type, String name) {
			this.origin = origin;
			this.type = type;
			this.name = name;
		}
	}
	
	public static enum Origin {
		HEDAER, QUERY, PATH, BODY, REQUEST;
	}
	
	public static class Primitives {
		public static int toint(String val) {
			return Integer.parseInt(val);
		}
		public static int toint(Object val) {
			return toInteger(val).intValue();
		}
		
		public static Integer toInteger(String val) {
			return new Integer(val);
		}
		public static Integer toInteger(Object val) {
			return (Integer)val;
		}
		
		public static long tolong(String val) {
			return Long.parseLong(val);
		}
		public static long tolong(Object val) {
			return toLong(val).longValue();
		}
		
		public static Long toLong(String val) {
			return new Long(val);
		}
		public static Long toLong(Object val) {
			return (Long) val;
		}
		
		public static String castStr(Argument arg, String param) {
			if(CtClass.intType.equals(arg.type) || CtClass.longType.equals(arg.type) || Integer.class.getName().equals(arg.type.getName()) || Long.class.getName().equals(arg.type.getName())) {
				return Primitives.class.getName() + ".to" + arg.type.getSimpleName() + "(" + param + ")";
			}
			return "(" + arg.type.getName() + ")" + param;
		}
	}

}
