package com.dtcoder.helloworld.controller;

import com.dtcoder.helloworld.service.HelloWorldService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello World REST 控制器。
 */
@RestController
@RequestMapping("/api")
public class HelloWorldController {

    private final HelloWorldService helloWorldService;

    public HelloWorldController(HelloWorldService helloWorldService) {
        this.helloWorldService = helloWorldService;
    }

    /**
     * 返回 Hello World 消息。
     *
     * @return Hello 消息
     */
    @GetMapping("/hello")
    public String hello() {
        return helloWorldService.getHelloMessage();
    }
}