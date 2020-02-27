package com.x.test;

import org.junit.Ignore;

import com.x.game.cmd.DirectModularCmd;
import com.x.game.cmd.QueuedCommand;
import com.x.game.player.Player;
import com.x.injection.Inject;
import com.x.net.cmd.Cmd;
import com.x.net.codec.IMessage;

@Ignore
@Cmd(101)
public class TestQueuedCommand extends QueuedCommand<Player> {
    
    @Inject
    private TTemplates templates;

	@Override
	public void exec(Player player, IMessage req) throws Exception {
		
	}
}