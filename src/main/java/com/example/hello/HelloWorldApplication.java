package com.example.hello;

/**
 * Hello World 应用入口
 *
 * @author DTCoder
 * @date 2025/01/16
 */
public class HelloWorldApplication {

    /**
     * 应用主入口
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        HelloWorldService service = new HelloWorldServiceImpl();
        String name = args.length > 0 ? args[0] : "World";
        System.out.println(service.greet(name).getMessage());
    }
}