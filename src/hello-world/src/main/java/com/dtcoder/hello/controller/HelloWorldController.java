package com.dtcoder.hello.controller;

import com.dtcoder.hello.HelloWorldService;
import com.dtcoder.hello.impl.HelloWorldServiceImpl;

/**
 * Hello World 控制器，作为应用入口
 *
 * @author DTCoder
 * @date 2025/07/11
 */
public class HelloWorldController {

    private final HelloWorldService helloWorldService;

    /**
     * 构造控制器，注入服务实例
     *
     * @param helloWorldService 问候服务
     */
    public HelloWorldController(HelloWorldService helloWorldService) {
        this.helloWorldService = helloWorldService;
    }

    /**
     * 程序入口：依次输出默认问候语和个性化问候语
     *
     * @param args 命令行参数，第一个参数作为用户名（可选）
     */
    public static void main(String[] args) {
        HelloWorldService service = new HelloWorldServiceImpl();
        HelloWorldController controller = new HelloWorldController(service);

        // 默认问候
        System.out.println(controller.helloWorldService.greet());

        // 个性化问候
        if (args.length > 0) {
            System.out.println(controller.helloWorldService.greet(args[0]));
        }
    }
}