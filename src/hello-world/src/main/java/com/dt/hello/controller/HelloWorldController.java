package com.dt.hello.controller;

import com.dt.hello.model.vo.HelloWorldResponse;
import com.dt.hello.service.HelloWorldService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello World REST 控制器。
 *
 * @author dtcoder
 */
@RestController
@RequestMapping("/api/hello")
public class HelloWorldController {

    private final HelloWorldService helloWorldService;

    public HelloWorldController(HelloWorldService helloWorldService) {
        this.helloWorldService = helloWorldService;
    }

    /**
     * 问候接口。
     *
     * @param name 可选名称，默认 "World"
     * @return 问候响应
     */
    @GetMapping
    public HelloWorldResponse greet(@RequestParam(defaultValue = "World") String name) {
        return helloWorldService.greet(name);
    }
}