package com.x.test;

import org.junit.Ignore;

import com.x.injection.Inject;
import com.x.injection.Loadable;
import com.x.injection.Templates;
@Ignore
@Templates
public class TTemplates implements Loadable {
    
    @Inject
    private TRepository repository;
    
    public void println() {
        System.out.println("I`m templates");
        repository.println();
    }

    @Override
    public void load() {
        System.out.println("Exec templates.load");
    }

}
