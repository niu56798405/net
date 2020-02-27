package com.x.test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.x.rpc.RpcInterface;

@RpcInterface
public interface GreetingService {
    String sayHello(String name);
    
    void print();
    
    int addInt(int a, Integer b);
    
    double addDouble(Double a, Double b);
    
    float addFloat(Float a, Float b);
    
    float addFloat(float a, Float b);

  
    long addLong(Long a);
    
    ConsumerApplication getConsumer(ConsumerApplication application);    
    
    List<ConsumerApplication> getConsumers(List<ConsumerApplication> applications);
    
    Set<ConsumerApplication> setTest(Set<ConsumerApplication> applications);

    
    void mapTest(Map<Integer, ConsumerApplication> map);
    
    
}