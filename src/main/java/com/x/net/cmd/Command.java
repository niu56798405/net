package com.x.net.cmd;

import com.x.net.codec.IMessage;
import com.x.net.session.Session;

public interface Command {

	void execute(Session session, IMessage req) throws Exception;

}
