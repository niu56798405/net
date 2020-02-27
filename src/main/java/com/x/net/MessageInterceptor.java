package com.x.net;

import com.x.net.codec.IMessage;
import com.x.net.session.Session;

/**
 * 拦截消息
 * @author 
 *
 */
public interface MessageInterceptor {
    
    /**
     * @param session 
     * @param code
     * @param req
     * @return True已被拦截 不做后续处理
     *          False 继续处理
     */
    public boolean intercept(Session session, IMessage req);
    
}
