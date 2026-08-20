package com.example.hello;

import com.example.hello.model.HelloWorldVO;

/**
 * Hello World 业务服务实现类
 *
 * @author DTCoder
 * @date 2025/01/16
 */
public class HelloWorldServiceImpl implements HelloWorldService {

    private static final String DEFAULT_NAME = "World";
    private static final String GREETING_PREFIX = "Hello, ";

    @Override
    public HelloWorldVO greet(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }

        String targetName = name.isEmpty() ? DEFAULT_NAME : name;
        String greeting = GREETING_PREFIX + targetName + "!";

        return new HelloWorldVO(greeting, greeting);
    }
}