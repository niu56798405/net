package com.x.http.service.rest;

import com.x.http.service.ServiceConfiguration;

public interface RestConfiguration extends ServiceConfiguration {

    RespEncoder getRespEncoder();

    BodyDecoder getBodyDecoder();

}
