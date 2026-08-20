package com.dtcoder.hello.impl;

import com.dtcoder.hello.HelloWorldService;

/**
 * Hello World 问候服务实现类
 *
 * @author DTCoder
 * @date 2025/07/11
 */
public class HelloWorldServiceImpl implements HelloWorldService {

    private static final String DEFAULT_GREETING = "Hello, World!";
    private static final String GREETING_TEMPLATE = "Hello, %s!";

    @Override
    public String greet() {
        return DEFAULT_GREETING;
    }

    @Override
    public String greet(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be null or blank");
        }
        return String.format(GREETING_TEMPLATE, name.trim());
    }
}