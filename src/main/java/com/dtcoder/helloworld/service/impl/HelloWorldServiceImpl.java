package com.dtcoder.helloworld.service.impl;

import com.dtcoder.helloworld.service.HelloWorldService;
import org.springframework.stereotype.Service;

/**
 * HelloWorldService 实现类。
 */
@Service
public class HelloWorldServiceImpl implements HelloWorldService {

    @Override
    public String getHelloMessage() {
        return "Hello, World!";
    }
}