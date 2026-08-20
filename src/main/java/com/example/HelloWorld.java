package com.example;

/**
 * HelloWorld 示例类，提供标准问候消息输出能力。
 *
 * <p>使用示例：
 * <pre>{@code
 *   HelloWorld hello = new HelloWorld();
 *   System.out.println(hello.getMessage());        // "Hello, World!"
 *   System.out.println(hello.getMessage("Alice"));  // "Hello, Alice!"
 * }</pre>
 *
 * @author DTCoder
 * @since 1.0.0
 */
public class HelloWorld {

    private static final String DEFAULT_NAME = "World";
    private static final String GREETING_PREFIX = "Hello, ";
    private static final String GREETING_SUFFIX = "!";

    /**
     * 程序入口。
     *
     * @param args 命令行参数，第一个参数将作为自定义名称
     */
    public static void main(String[] args) {
        HelloWorld helloWorld = new HelloWorld();
        if (args.length > 0) {
            System.out.println(helloWorld.getMessage(args[0]));
        } else {
            System.out.println(helloWorld.getMessage());
        }
    }

    /**
     * 获取默认问候消息。
     *
     * @return 默认问候消息 "Hello, World!"
     */
    public String getMessage() {
        return getMessage(DEFAULT_NAME);
    }

    /**
     * 获取指定名称的问候消息。
     *
     * @param name 名称，不能为 null 或空白
     * @return 问候消息，格式为 "Hello, {name}!"
     * @throws IllegalArgumentException 当 name 为 null 或空白时抛出
     */
    public String getMessage(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be null or blank");
        }
        return GREETING_PREFIX + name + GREETING_SUFFIX;
    }
}