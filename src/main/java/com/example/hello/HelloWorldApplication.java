package com.example.hello;

import com.example.hello.service.HelloWorldService;
import com.example.hello.service.impl.HelloWorldServiceImpl;

/**
 * Hello World 应用入口。
 *
 * <p>演示 {@link HelloWorldService} 的基本用法。</p>
 */
public class HelloWorldApplication {

    private static final String DEFAULT_ARG_NAME = "World";

    /**
     * 应用主方法。
     *
     * @param args 命令行参数，args[0] 为可选名称
     */
    public static void main(String[] args) {
        HelloWorldService helloWorldService = new HelloWorldServiceImpl();

        String name = (args.length > 0) ? args[0] : DEFAULT_ARG_NAME;
        String greeting = helloWorldService.greet(name);

        System.out.println(greeting);
    }
}