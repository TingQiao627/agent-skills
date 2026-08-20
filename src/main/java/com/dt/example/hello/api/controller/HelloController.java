package com.dt.example.hello.api.controller;

import com.dt.example.hello.api.response.HelloVO;
import com.dt.example.hello.common.response.ApiResponse;
import com.dt.example.hello.service.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 问候控制器。
 *
 * <p>暴露 REST 接口，提供问候服务。</p>
 */
@RestController
@RequestMapping("/hello")
public class HelloController {

    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    private final HelloService helloService;

    public HelloController(HelloService helloService) {
        this.helloService = helloService;
    }

    /**
     * 获取问候语。
     *
     * <p>GET /hello — 返回 "Hello World" 问候信息。</p>
     *
     * @return 包含问候语的统一响应
     */
    @GetMapping
    public ApiResponse<HelloVO> sayHello() {
        if (logger.isDebugEnabled()) {
            logger.debug("GET /hello invoked");
        }
        String greeting = helloService.sayHello();
        HelloVO vo = HelloVO.of(greeting);
        return ApiResponse.success(vo);
    }
}