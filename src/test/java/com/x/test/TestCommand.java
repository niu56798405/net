package com.x.test;

import org.junit.Ignore;

import com.x.game.cmd.DirectModularCmd;
import com.x.injection.Inject;
import com.x.modular.Modular;
import com.x.net.cmd.Cmd;
import com.x.net.codec.IMessage;

@Ignore
@Cmd(100)
public class TestCommand extends DirectModularCmd<TestPlayer, TPlayerInventory> {
    
    @Inject
    private TTemplates templates;

    @Override
    public void exec(TestPlayer player, TPlayerInventory module, IMessage req) throws Exception {
        player.player.println();
        System.out.println("-------------start exec-------------");
        System.out.println(this);
        System.out.println(player);
        System.out.println(module);
        System.out.println("-------------stop  exec-------------");
        
        templates.println();
    }
	
}