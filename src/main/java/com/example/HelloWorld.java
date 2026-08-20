package com.example;

import com.example.service.HelloWorldService;
import com.example.service.impl.HelloWorldServiceImpl;

/**
 * Hello World 程序入口。
 *
 * <p>演示数科 Java 编码规范下的标准工程结构，包含接口-实现分离、Javadoc 注释、
 * 命名规范等核心实践。
 *
 * @author dtcoder
 * @since 1.0.0
 */
public class HelloWorld {

    private static final String DEFAULT_NAME = HelloWorldService.DEFAULT_NAME;

    /**
     * 程序主入口。
     *
     * @param args 命令行参数，第一个参数作为问候对象名称（可选）
     */
    public static void main(String[] args) {
        HelloWorldService service = new HelloWorldServiceImpl();
        String name = (args.length > 0) ? args[0] : DEFAULT_NAME;
        System.out.println(service.getGreeting(name));
    }
}