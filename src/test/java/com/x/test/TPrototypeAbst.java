package com.x.test;

import com.x.injection.Inject;
import com.x.injection.Prototype;


@Prototype
public class TPrototypeAbst {
    
    @Inject
    private TRepository tRepository;
    
    public void println() {
        System.out.println("I`m tprototypeabst " + tRepository);
    }
    

}
