package com.dt.example.hello.common.constant;

/**
 * Hello World 模块常量定义。
 *
 * @author hello-world-module
 */
public final class HelloWorldConstants {

    private HelloWorldConstants() {
        throw new UnsupportedOperationException("常量类不允许实例化");
    }

    /** 默认问候前缀 */
    public static final String DEFAULT_GREETING_PREFIX = "Hello, ";

    /** 默认名称（当用户未提供名称时使用） */
    public static final String DEFAULT_NAME = "World";

    /** 问候语后缀 */
    public static final String GREETING_SUFFIX = "!";
}