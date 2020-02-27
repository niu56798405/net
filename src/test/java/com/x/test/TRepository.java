package com.x.test;

import org.junit.Ignore;

import com.x.injection.Repository;

@Ignore
@Repository
public class TRepository extends TAbsRepo {
    
    public void println() {
        jdbcTemplate.println();
        System.out.println("I`m repository");
    }

}
