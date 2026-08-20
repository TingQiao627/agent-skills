package com.dt.example.helloworld.service;

/**
 * Hello World 业务服务接口
 *
 * <p>提供欢迎消息生成能力。
 */
public interface HelloWorldService {

    /**
     * 根据名称生成欢迎消息
     *
     * <p>若名称为 null、空字符串或仅包含空白字符，返回默认欢迎消息 "Hello, World!"。
     * 名称长度不得超过 100 个字符。
     *
     * @param name 用户名称，可为 null
     * @return 欢迎消息
     * @throws IllegalArgumentException 若名称长度超过 100 个字符
     */
    String greet(String name);
}