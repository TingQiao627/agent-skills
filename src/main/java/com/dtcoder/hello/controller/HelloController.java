package com.dtcoder.hello.controller;

import com.dtcoder.hello.model.dto.HelloResponse;
import com.dtcoder.hello.service.HelloService;
import com.dtcoder.hello.service.impl.HelloServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello 问候 REST 控制器
 *
 * @author DTCoder
 * @date 2025/01/20
 */
public class HelloController {

    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    private final HelloService helloService;

    /**
     * 构造方法，注入 HelloService
     */
    public HelloController() {
        this.helloService = new HelloServiceImpl();
    }

    /**
     * 构造方法，支持依赖注入
     *
     * @param helloService Hello 服务实例
     */
    public HelloController(HelloService helloService) {
        this.helloService = helloService;
    }

    /**
     * 处理问候请求
     *
     * @param name 请求来源名称，可选
     * @return 包含问候消息的响应对象
     */
    public HelloResponse greet(String name) {
        if (logger.isDebugEnabled()) {
            logger.debug("Received greet request with name: {}", name);
        }

        String greeting = helloService.getGreeting(name);
        HelloResponse response = new HelloResponse(greeting, name);

        if (logger.isDebugEnabled()) {
            logger.debug("Returning greet response: {}", response);
        }
        return response;
    }
}