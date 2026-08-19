package com.example.helloworld;

import com.example.helloworld.service.HelloWorldService;
import com.example.helloworld.service.impl.HelloWorldServiceImpl;

/**
 * Hello World 应用程序入口
 *
 * @author dtcoder
 * @date 2025/07/10
 */
public class HelloWorldApplication {

    /**
     * 程序主入口
     *
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        HelloWorldService helloWorldService = new HelloWorldServiceImpl();
        System.out.println(helloWorldService.getMessage());
    }
}