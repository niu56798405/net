package com.x.test;

import com.x.injection.Inject;

public class TPrototype extends TPrototypeAbst {
    
    @Inject
    private TTemplates templates;
    
    public void println() {
        super.println();
        System.out.println("I`m tprototype " + templates);
    }
    
}
