package com.example.hello.service.impl;

import com.example.hello.service.HelloWorldService;

/**
 * {@link HelloWorldService} 的默认实现。
 *
 * <p>对 {@code null}、空字符串、空白字符串输入返回默认问候语 "Hello, World!"。</p>
 */
public class HelloWorldServiceImpl implements HelloWorldService {

    private static final String DEFAULT_NAME = "World";
    private static final String GREETING_TEMPLATE = "Hello, %s!";

    @Override
    public String greet(String name) {
        String effectiveName = (name == null || name.isBlank()) ? DEFAULT_NAME : name.trim();
        return String.format(GREETING_TEMPLATE, effectiveName);
    }
}