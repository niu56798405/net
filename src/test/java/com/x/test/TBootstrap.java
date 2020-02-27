package com.x.test;

import java.util.List;

import com.x.game.GameInitializer;
import com.x.game.player.PlayerContext;
import com.x.game.player.callable.ModularCallable;
import com.x.http.HttpServer;
import com.x.http.service.Http;
import com.x.http.service.Request;
import com.x.http.service.Response;
import com.x.http.service.Service;
import com.x.http.service.ServiceContext;
import com.x.injection.ApplicationContext;
import com.x.injection.code.Codes;
import com.x.modular.ModularType;
import com.x.net.cmd.CommandContext;

public class TBootstrap {
	
	public static void main(String[] args) throws Exception {
        GameInitializer.initialize(TestPlayer.class, 1);
		ApplicationContext.registBean("defaultJdbcTemplate", new TJdbcTemplate());
		ApplicationContext.initialize("com.x.*", "*.jar");
		
		TestPlayer player = ApplicationContext.fetchBean(PlayerContext.class).getPlayerImmediately(10086);
		
		player.load(ModularType.TRANSIENT);
        
        TestCommand object = (TestCommand) ApplicationContext.fetchBean(CommandContext.class).get((short) 100);
        object.execute(player, null);
        
        player.player.println();
        
        new ModularCallable<TestPlayer, TPlayerInventory>() {
            @Override
            public void call(TestPlayer player, TPlayerInventory module) {
                System.out.println("I`m callable : " + player + "\t" + module);
            }
        }.call(player);
        
        player.save();
        player.unload(ModularType.TRANSIENT);
		
		new TPrototype().println();
		
		new TPlayerInfo().println();
		
		final ServiceContext ctx = ApplicationContext.fetchBean(ServiceContext.class);
        ctx.registService("test", new Service() {
            @Override
            public Response service(Request req) {
            	List<Class<?>> increase = Codes.getClasses4Increase(Http.class);
            	ctx.finalize(increase);
				List<Class<?>> replace = Codes.getClasses4Replace(Http.class);
				ctx.finalize(replace);
                return new Response(increase.toString() + "\n" + replace.toString()+ "helloworld");
            }
        });
        HttpServer server = new HttpServer();
        server.start(8090, ctx);
	}

}
