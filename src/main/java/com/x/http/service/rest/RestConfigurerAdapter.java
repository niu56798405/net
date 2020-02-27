package com.x.http.service.rest;

import com.x.http.service.FileHandler;
import com.x.http.service.RequestInteceptor;
import com.x.http.service.Response;
import com.x.http.service.ServiceErrorHandler;
import com.x.injection.Bean;
import com.x.injection.Loadable;
import com.x.tools.Strings;

//优先加载, 后来的覆盖此类
@Bean(backup=true)
public class RestConfigurerAdapter implements RestConfiguration, Loadable {
    
    private RequestInteceptor inteceptor;
    private ServiceErrorHandler errorhandler;
    private FileHandler fileHandler;
    
    private BodyDecoder bodyDecoder;
    private RespEncoder respEncoder;
    
    @Override
    public final RequestInteceptor getInteceptor() {
        return inteceptor;
    }

    @Override
    public final ServiceErrorHandler getErrorhandler() {
        return errorhandler;
    }

    @Override
    public final BodyDecoder getBodyDecoder() {
        return bodyDecoder;
    }

    @Override
    public final RespEncoder getRespEncoder() {
        return respEncoder;
    }
    
    @Override
    public final FileHandler getFileHandler() {
        return fileHandler;
    }

    @Override
    public final void load() {
        setIncepetor(v->inteceptor=v);
        setErrorHandler(v->errorhandler=v);
        setFileHandler(v->fileHandler=v);
        
        setBodyDecoder(v->bodyDecoder=v);
        setRespEncoder(v->respEncoder=v);
    }

    public void setIncepetor(IncepetorSetter setter) {
        setter.set(r -> null);
    }

    public void setErrorHandler(ErrorHandlerSetter setter) {
        setter.set((r, e) -> new Response(Strings.getStackTrace(e)));
    }

    public void setBodyDecoder(BodyDecoderSetter setter) {
        setter.set((t, b) -> b);
    }

    public void setRespEncoder(RespEncoderSetter setter) {
        setter.set(o -> (Response) o);
    }

    public void setFileHandler(FileHandlerSetter setter) {
        setter.set(p -> p);
    }
    
    public interface IncepetorSetter {
        void set(RequestInteceptor inteceptor);
    }
    public interface BodyDecoderSetter {
        void set(BodyDecoder bodyDecoder);
    }
    public interface RespEncoderSetter {
        void set(RespEncoder respEncoder);
    }
    public interface FileHandlerSetter {
        void set(FileHandler fileHandler);
    }
    public interface ErrorHandlerSetter {
        void set(ServiceErrorHandler errorHandler);
    }
}
