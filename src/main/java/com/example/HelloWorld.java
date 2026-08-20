package com.example;

/**
 * HelloWorld 问候服务类。
 *
 * <p>提供默认问候和个性化问候功能。</p>
 *
 * @author dtcoder
 * @since 1.0.0
 */
public class HelloWorld {

    /** 默认问候语模板。 */
    private static final String GREETING_TEMPLATE = "Hello, %s!";

    /** 默认名称。 */
    private static final String DEFAULT_NAME = "World";

    /**
     * 返回默认问候语 "Hello, World!"。
     *
     * @return 默认问候语字符串
     */
    public String greet() {
        return String.format(GREETING_TEMPLATE, DEFAULT_NAME);
    }

    /**
     * 返回个性化问候语 "Hello, {name}!"。
     *
     * @param name 问候对象名称，不能为 null 或空白
     * @return 个性化问候语字符串
     * @throws IllegalArgumentException 当 name 为 null 或空白时抛出
     */
    public String greet(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be null or blank");
        }
        return String.format(GREETING_TEMPLATE, name);
    }
}