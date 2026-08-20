package com.dt.example.helloworld.controller;

import com.dt.example.helloworld.service.HelloWorldService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello World REST 控制器
 *
 * <p>提供 HTTP 接口用于获取欢迎消息。
 */
@RestController
@RequestMapping("/api/hello")
public class HelloWorldController {

    private final HelloWorldService helloWorldService;

    /**
     * 构造器注入 HelloWorldService
     *
     * @param helloWorldService 业务服务
     */
    public HelloWorldController(HelloWorldService helloWorldService) {
        this.helloWorldService = helloWorldService;
    }

    /**
     * 获取欢迎消息
     *
     * @param name 用户名称，可选参数
     * @return 欢迎消息字符串
     */
    @GetMapping
    public String greet(@RequestParam(required = false) String name) {
        return helloWorldService.greet(name);
    }
}