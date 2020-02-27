package com.x.test;

import org.junit.Ignore;

import com.x.injection.Inject;
@Ignore
public abstract class TAbsRepo {

    @Inject("defaultJdbcTemplate")
    protected TJdbcTemplate jdbcTemplate;
    
}
