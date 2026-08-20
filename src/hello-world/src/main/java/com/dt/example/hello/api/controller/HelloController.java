package com.dt.example.hello.api.controller;

import com.dt.example.hello.model.vo.HelloVO;
import com.dt.example.hello.service.HelloService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello REST 控制器。
 *
 * @author dtcoder
 */
@RestController
@RequestMapping("/api/hello")
public class HelloController {

    private final HelloService helloService;

    /**
     * 构造注入。
     *
     * @param helloService Hello 服务
     */
    public HelloController(HelloService helloService) {
        this.helloService = helloService;
    }

    /**
     * 获取问候语。
     *
     * @param name 名称，默认值为 "World"
     * @return 包含问候消息的视图对象
     */
    @GetMapping
    public HelloVO sayHello(@RequestParam(defaultValue = "World") String name) {
        return helloService.sayHello(name);
    }
}