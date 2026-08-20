package com.dtcoder.hello.controller;

import com.dtcoder.hello.service.HelloService;
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
@RequestMapping("/api")
public class HelloController {

    private final HelloService helloService;

    public HelloController(HelloService helloService) {
        this.helloService = helloService;
    }

    /**
     * 返回问候语。
     *
     * @param name 可选名称参数
     * @return 问候语字符串
     */
    @GetMapping("/hello")
    public String hello(@RequestParam(required = false) String name) {
        return helloService.greet(name);
    }
}