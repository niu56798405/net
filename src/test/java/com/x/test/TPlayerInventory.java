package com.x.test;

import org.junit.Ignore;

import com.x.injection.Inject;
import com.x.modular.Modular;
import com.x.modular.ModularType;

@Ignore
@Modular(ModularType.RESIDENT)
public class TPlayerInventory extends TInventory implements TSharablePlayer {
	@Inject
    private TRepository repository;
	private TestPlayer player;
	
	public boolean load(TestPlayer player) {
		this.player = player;
		System.out.println("modular[player inventory] loaded by : " + this.player);
		return true;
	}
	
	public boolean save() {
		System.out.println("player inventory saved !!!");
		return true;
	}
	
    @Override
    public TSharablePlayer share() {
        return this;
    }

    @Override
    public void println() {
        repository.println();
        System.out.println("sharable methods!!!");
    }

}
