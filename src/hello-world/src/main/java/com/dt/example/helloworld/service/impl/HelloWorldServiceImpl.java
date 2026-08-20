package com.dt.example.helloworld.service.impl;

import com.dt.example.helloworld.service.HelloWorldService;

/**
 * Hello World 业务服务实现
 *
 * <p>提供欢迎消息生成的默认实现。
 */
public class HelloWorldServiceImpl implements HelloWorldService {

    /** 默认欢迎消息 */
    private static final String DEFAULT_GREETING = "Hello, World!";

    /** 名称最大长度 */
    private static final int MAX_NAME_LENGTH = 100;

    /**
     * 根据名称生成欢迎消息
     *
     * <p>若名称为 null、空字符串或仅包含空白字符，返回默认欢迎消息。
     *
     * @param name 用户名称
     * @return 欢迎消息
     * @throws IllegalArgumentException 若名称长度超过 {@value #MAX_NAME_LENGTH} 个字符
     */
    @Override
    public String greet(String name) {
        if (name != null && name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    "Name length must not exceed " + MAX_NAME_LENGTH + " characters");
        }
        if (name == null || name.isBlank()) {
            return DEFAULT_GREETING;
        }
        return "Hello, " + name.trim() + "!";
    }
}