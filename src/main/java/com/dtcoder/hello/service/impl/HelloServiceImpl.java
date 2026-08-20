package com.dtcoder.hello.service.impl;

import com.dtcoder.hello.service.HelloService;
import org.springframework.stereotype.Service;

/**
 * 问候服务实现。
 *
 * @author dtcoder
 */
@Service
public class HelloServiceImpl implements HelloService {

    private static final String DEFAULT_GREETING = "Hello, World!";
    private static final String GREETING_TEMPLATE = "Hello, %s!";

    @Override
    public String greet(String name) {
        if (name == null || name.isBlank()) {
            return DEFAULT_GREETING;
        }
        return String.format(GREETING_TEMPLATE, name.trim());
    }
}