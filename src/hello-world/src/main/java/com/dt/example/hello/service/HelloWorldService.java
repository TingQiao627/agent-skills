package com.dt.example.hello.service;

/**
 * 问候服务接口。
 *
 * @author hello-world-module
 */
public interface HelloWorldService {

    /**
     * 根据传入的名称返回问候语。
     * 当名称为 {@code null} 或空白时，使用默认名称 "World"。
     *
     * @param name 用户名称，可为 {@code null}
     * @return 格式化的问候语，如 "Hello, World!"
     */
    String greet(String name);
}