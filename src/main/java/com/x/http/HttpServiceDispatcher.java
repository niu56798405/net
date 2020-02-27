package com.x.http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.x.http.service.Request;
import com.x.http.service.RequestInteceptor;
import com.x.http.service.Response;
import com.x.http.service.ServiceContext;
import com.x.http.service.ServiceContext.ServiceInvoker;
import com.x.tools.Mimetypes;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;

/**
 * 
 * Dispatch http service methods
 * @author 
 *
 */
public class HttpServiceDispatcher extends SimpleChannelInboundHandler<Object> {
    
    private final static Logger logger = LoggerFactory.getLogger(HttpServer.class);
    
    private final ServiceContext ctx;
    
    public HttpServiceDispatcher(ServiceContext ctx) {
        this.ctx = ctx;
    }
    
    private HttpRequest request;
    private Request req;

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest request = this.request = (HttpRequest) msg;
            this.req = new Request(((InetSocketAddress)(ctx.channel().remoteAddress())).getAddress(), request.getUri(), request.headers(), request.getMethod());
            if (HttpHeaders.is100ContinueExpected(request)) {
                send100Continue(ctx);
            }

            req.appendDecoderResult(request.getDecoderResult());
        }

       if (msg instanceof HttpContent) {
           HttpContent httpContent = (HttpContent) msg;
           ByteBuf content = httpContent.content();
           
           if (content.isReadable()) {
               req.appendContent(content);
               req.appendDecoderResult(request.getDecoderResult());
           }
           
           if (msg instanceof LastHttpContent) {
               service(ctx);
           }
       }
   }

   private void service(ChannelHandlerContext ctx) {
       if(req.isSuccess()) {
           RequestInteceptor interceptor = this.ctx.Interceptor();
           Response intecept = interceptor.intecept(req);
           if(intecept != null) {
               sendResponse(ctx, intecept);
               return;
           }

           ServiceInvoker invoker = this.ctx.get(req.path());
           if(invoker == null) {
               sendAsFile(ctx);
           } else {
               doService(ctx, invoker);
           }
       } else {
           sendBadRequestResponse(ctx);
       }
   }

    protected void doService(ChannelHandlerContext ctx, ServiceInvoker invoker) {
        try {
            sendResponse(ctx, invoker.invoke(req));
        } catch (Exception ex) {
            sendResponse(ctx, this.ctx.errorHandler().handle(req, ex));
        }
    }

    protected void sendAsFile(ChannelHandlerContext ctx) {
        String file = this.ctx.fileHandler().getPath(req.path());
        if(file != null) {
            try {
                sendFileResponse(ctx, file); 
            } catch (Exception ex) {
                sendResponse(ctx, this.ctx.errorHandler().handle(req, ex));
            }
        } else {
            sendNotFoudResponse(ctx);
        }
    }

   protected void sendResponse(ChannelHandlerContext ctx, Response resp) {
       sendContentResponse(ctx, HttpResponseStatus.OK, resp);
   }

   protected void sendNotFoudResponse(ChannelHandlerContext ctx) {
       logger.debug(req.path() + " not found");
       sendContentResponse(ctx, HttpResponseStatus.NOT_FOUND, Response.NOT_FOUND);
   }

   protected void sendBadRequestResponse(ChannelHandlerContext ctx) {
       sendContentResponse(ctx, HttpResponseStatus.BAD_REQUEST, Response.BAD_REQUEST);
   }

   protected void sendContentResponse(ChannelHandlerContext ctx, HttpResponseStatus status, Response resp) {
       // Build the response object.
       FullHttpResponse response = new DefaultFullHttpResponse(
               HttpVersion.HTTP_1_1, 
               status,
               resp.content);

       setContentType(response, resp.type.val);

       // Write the response.
       ctx.write(response);
       ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
   }

    protected void setContentType(HttpResponse response, String contentType) {
        response.headers().set(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, contentType);
        response.headers().set(HttpHeaders.Names.CONTENT_ENCODING, "UTF-8");
    }

   public static final String HTTP_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
   public static final int HTTP_CACHE_SECONDS = 60;
   
   @SuppressWarnings("resource")
   protected void sendFileResponse(ChannelHandlerContext ctx, String path) throws Exception {
       File file = new File(path);
       if (!file.exists() || !file.isFile() || file.isHidden()) {
           sendNotFoudResponse(ctx);
           return;
       }

       // Cache Validation
       String ifModifiedSince = request.headers().get(HttpHeaders.Names.IF_MODIFIED_SINCE);
       if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
           SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT);
           Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);

           // Only compare up to the second because the datetime format we send to the client
           // does not have milliseconds
           long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
           long fileLastModifiedSeconds = file.lastModified() / 1000;
           if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
               sendNotModified(ctx);
               return;
           }
       }

       RandomAccessFile raf;
       try {
           raf = new RandomAccessFile(file, "r");
       } catch (FileNotFoundException ignore) {
           sendNotFoudResponse(ctx);
           return;
       }
       long fileLength = raf.length();

       HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
       
       setContentType(response, Mimetypes.get(file.getName()));
       
       response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, fileLength);
       
       setDateAndCacheHeaders(response, file);
       // Write the initial line and the header.
       ctx.write(response);

       if(!HttpMethod.HEAD.equals(request.getMethod())) {
           // Write the content.
           ctx.write(new DefaultFileRegion(raf.getChannel(), 0, fileLength), ctx.newProgressivePromise());
       }
       // Write the end marker.
       // Close the connection when the whole content is written out.
       ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE);
   }

   protected void send100Continue(ChannelHandlerContext ctx) {
       FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
       ctx.write(response);
   }

   @Override
   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
       ctx.close();
   }
   
   protected void sendNotModified(ChannelHandlerContext ctx) {
       FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_MODIFIED);
       SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT);

       Calendar time = new GregorianCalendar();
       response.headers().set(HttpHeaders.Names.DATE, dateFormatter.format(time.getTime()));

       // Close the connection as soon as the error message is sent.
       ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
   }

   protected void setDateAndCacheHeaders(HttpResponse response, File fileToCache) {
       SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT);

       // Date header
       Calendar time = new GregorianCalendar();
       response.headers().set(HttpHeaders.Names.DATE, dateFormatter.format(time.getTime()));

       // Add cache headers
       time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
       response.headers().set(HttpHeaders.Names.EXPIRES, dateFormatter.format(time.getTime()));
       response.headers().set(HttpHeaders.Names.CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
       response.headers().set(HttpHeaders.Names.LAST_MODIFIED, dateFormatter.format(new Date(fileToCache.lastModified())));
   }

}
