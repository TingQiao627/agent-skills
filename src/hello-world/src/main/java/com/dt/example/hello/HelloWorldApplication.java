package com.dt.example.hello;

/**
 * Hello World 应用入口
 *
 * @author DTCoder
 * @date 2025/01/20
 */
public class HelloWorldApplication {

    /**
     * 应用主入口
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        GreetingService greetingService = new GreetingServiceImpl();

        // 默认问候
        System.out.println(greetingService.greet(null));

        // 带名称问候
        System.out.println(greetingService.greet("World"));
    }
}