package com.example.helloworld.service.impl;

import com.example.helloworld.service.HelloWorldService;

/**
 * {@link HelloWorldService} 的实现类。
 *
 * @author helloworld
 */
public class HelloWorldServiceImpl implements HelloWorldService {

    @Override
    public String greet(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return "Hello, " + name + "!";
    }
}