package com.dtstack.helloworld;

import com.dtstack.helloworld.service.HelloWorldService;
import com.dtstack.helloworld.service.impl.HelloWorldServiceImpl;

/**
 * Hello World 应用程序入口。
 *
 * @author dtcoder
 */
public class HelloWorldApplication {

    public static void main(String[] args) {
        HelloWorldService helloWorldService = new HelloWorldServiceImpl();

        String name = args.length > 0 ? args[0] : null;
        String greeting = helloWorldService.greet(name);

        System.out.println(greeting);
    }
}