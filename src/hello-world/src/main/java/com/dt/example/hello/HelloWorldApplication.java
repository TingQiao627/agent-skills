package com.dt.example.hello;

import com.dt.example.hello.service.HelloWorldService;
import com.dt.example.hello.service.impl.HelloWorldServiceImpl;

/**
 * Hello World 应用入口。
 *
 * @author hello-world-module
 */
public class HelloWorldApplication {

    public static void main(String[] args) {
        HelloWorldService service = new HelloWorldServiceImpl();
        String name = (args.length > 0) ? args[0] : "World";
        System.out.println(service.greet(name));
    }
}