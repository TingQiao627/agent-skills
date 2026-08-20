package com.example;

/**
 * Hello World 示例类，提供问候语生成功能。
 *
 * @author DTCoder
 */
public class HelloWorld {

    private static final String DEFAULT_NAME = "World";
    private static final String GREETING_TEMPLATE = "Hello, %s!";

    /**
     * 返回默认问候语 "Hello, World!"。
     *
     * @return 默认问候语
     */
    public String greet() {
        return greet(DEFAULT_NAME);
    }

    /**
     * 返回个性化问候语。
     *
     * @param name 问候对象名称，为 null 或空字符串时使用默认值
     * @return 格式化后的问候语
     */
    public String greet(String name) {
        String resolvedName = (name == null || name.isEmpty()) ? DEFAULT_NAME : name;
        return String.format(GREETING_TEMPLATE, resolvedName);
    }
}