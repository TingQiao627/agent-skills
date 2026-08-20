package com.example.helloworld;

import com.example.helloworld.service.HelloWorldService;
import com.example.helloworld.service.impl.HelloWorldServiceImpl;

/**
 * Hello World 程序入口。
 *
 * @author helloworld
 */
public class Main {

    public static void main(String[] args) {
        HelloWorldService service = new HelloWorldServiceImpl();
        String name = args.length > 0 ? args[0] : "World";
        System.out.println(service.greet(name));
    }
}