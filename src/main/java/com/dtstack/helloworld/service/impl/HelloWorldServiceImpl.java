package com.dtstack.helloworld.service.impl;

import com.dtstack.helloworld.service.HelloWorldService;

/**
 * {@link HelloWorldService} 的实现类。
 *
 * @author dtcoder
 */
public class HelloWorldServiceImpl implements HelloWorldService {

    private static final String DEFAULT_GREETING = "Hello, World!";
    private static final String GREETING_PREFIX = "Hello, ";

    @Override
    public String greet(String name) {
        if (name == null || name.isBlank()) {
            return DEFAULT_GREETING;
        }
        return GREETING_PREFIX + name.trim() + "!";
    }
}