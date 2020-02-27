package com.x.http.service;

import com.x.injection.Combine;

@Combine
public interface RequestInteceptor {
    
    Response intecept(Request req);

}
