package com.x.test;

import com.x.modular.ModularLoad;
import com.x.modular.ModularSave;
import com.x.modular.ModularUnload;
import com.x.modular.Module;

public abstract class TInventory implements Module<TSharablePlayer> {

    @ModularLoad("this")
    public abstract boolean load(TestPlayer player);
    @ModularUnload @ModularSave
    public abstract boolean save();
    
    public abstract TSharablePlayer share();
    
}
