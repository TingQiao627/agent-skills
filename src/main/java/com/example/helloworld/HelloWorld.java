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

    /** 中文默认问候语 */
    private static final String DEFAULT_CHINESE_GREETING = "你好，世界！";

    /**
     * 获取默认问候语。
     *
     * @return 问候语字符串
     */
    public String getGreeting() {
        return DEFAULT_GREETING;
    }

    /**
     * 获取中文默认问候语。
     *
     * @return 中文问候语字符串
     */
    public String getChineseGreeting() {
        return DEFAULT_CHINESE_GREETING;
    }

    /**
     * 获取指向指定对象的问候语。
     *
     * @param name 被问候的对象名称
     * @return 包含对象名称的问候语字符串
     */
    public String getGreeting(String name) {
        String target = (name == null || name.isBlank()) ? "World" : name.trim();
        return "Hello, " + target + "!";
    }

    /**
     * 获取指向指定对象的中文问候语。
     *
     * @param name 被问候的对象名称
     * @return 包含对象名称的中文问候语字符串
     */
    public String getChineseGreeting(String name) {
        String target = (name == null || name.isBlank()) ? "世界" : name.trim();
        return "你好，" + target + "！";
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