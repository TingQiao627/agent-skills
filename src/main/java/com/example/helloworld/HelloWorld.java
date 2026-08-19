package com.example.helloworld;

/**
 * HelloWorld 示例程序，输出标准问候语。
 *
 * @author dtcoder
 * @date 2025/01/20
 */
public class HelloWorld {

    /** 默认问候语 */
    private static final String DEFAULT_GREETING = "Hello, World!";

    /**
     * 获取默认问候语。
     *
     * @return 问候语字符串
     */
    public String getGreeting() {
        return DEFAULT_GREETING;
    }

    /**
     * 程序入口，向标准输出打印问候语。
     *
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        HelloWorld helloWorld = new HelloWorld();
        System.out.println(helloWorld.getGreeting());
    }
}