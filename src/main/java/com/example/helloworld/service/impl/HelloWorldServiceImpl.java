package com.example.helloworld.service.impl;

import com.example.helloworld.service.HelloWorldService;

/**
 * Hello World 服务实现类
 *
 * @author dtcoder
 * @date 2025/07/10
 */
public class HelloWorldServiceImpl implements HelloWorldService {

    /** 默认问候消息 */
    private static final String DEFAULT_GREETING = "Hello, World!";

    /**
     * 获取问候消息
     *
     * @return 默认问候消息 "Hello, World!"
     */
    @Override
    public String getMessage() {
        return DEFAULT_GREETING;
    }
}