package com.x.net.kcp.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.x.injection.ApplicationContext;
import com.x.net.LifecycleListener;
import com.x.net.cmd.Command;
import com.x.net.cmd.CommandContext;
import com.x.net.codec.IMessage;
import com.x.net.codec.MessageUtil;
import com.x.net.kcp.IKcpServer;
import com.x.net.kcp.KcpOnUdp;
import com.x.net.session.Session;

import io.netty.buffer.ByteBuf;

/**
 *
 * @author
 */
public class KcpServer extends IKcpServer{
		
  private Logger logger = LoggerFactory.getLogger(KcpServer.class);
    
  private CommandContext cmds;
  

  
  public KcpServer(int port, LifecycleListener listener)
  {	 
	  super(port, Runtime.getRuntime().availableProcessors() * 2, listener);
	  this.cmds = ApplicationContext.fetchBean(CommandContext.class);
  }
    
  public KcpServer(int port, int workerSize, LifecycleListener listener, CommandContext cmds)
  {
    super(port, workerSize, listener);
    this.cmds = cmds;
  }

  @Override
  
  public void handleReceive(ByteBuf bb, KcpOnUdp kcp){ 	   
	
	  exec(kcp.getKcpSession(), MessageUtil.decode(bb));
  }

  @Override
  public void handleException(Throwable ex, KcpOnUdp kcp){
	ex.printStackTrace();  	
    logger.error(ex.toString());
  }

  @Override
  public void handleClose(KcpOnUdp kcp)
  {
	  logger.info("客户端离开:" + kcp);
	  logger.info("waitSnd:" + kcp.getKcp().waitSnd());
	  listener.onSessionUnRegister(kcp.getKcpSession());
  }
  

  private void exec(Session session, IMessage req) {      
      
	  listener.onMessageRecieve(session, req);
      
      short code = req.getCode();
      Command cmd = cmds.get(code);      
      if(cmd == null) {
          logger.warn("Can't find the command: " + code);
          return;
      }
      
      try {
	        cmd.execute(session, req);
	    } catch (Throwable ex) {
	        listener.onCmdException(session, cmd, req, ex);
	    }
  }
  
  
  /**
 * 使用 默认参数设置kcp
 */
  public void start(){
	  this.noDelay(1, 10, 2, 1);	   
	  this.setMinRto(10);	   	
	  this.wndSize(32, 32);	   	
	  this.setTimeout(10 * 1000);
	  this.setMtu(256);	
	  logger.info("kcp服务器初始化 设置完成 ");
	  super.start();
  }
  

}
