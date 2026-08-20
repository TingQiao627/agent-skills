package com.example.hello;

import com.example.hello.model.HelloWorldVO;

/**
 * Hello World REST 控制器
 * 暴露 HTTP 接口供外部调用
 *
 * @author DTCoder
 * @date 2025/01/16
 */
public class HelloWorldController {

    private final HelloWorldService helloWorldService;

    /**
     * 构造器注入
     *
     * @param helloWorldService 问候服务
     */
    public HelloWorldController(HelloWorldService helloWorldService) {
        this.helloWorldService = helloWorldService;
    }

    /**
     * 获取问候语
     *
     * @param name 被问候者名称
     * @return 包含问候信息的视图对象
     */
    public HelloWorldVO greet(String name) {
        return helloWorldService.greet(name);
    }
}