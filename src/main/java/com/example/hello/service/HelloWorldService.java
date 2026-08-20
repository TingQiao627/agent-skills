package com.example.hello.service;

/**
 * 问候服务接口。
 *
 * <p>提供基于名称的问候语生成能力。</p>
 */
public interface HelloWorldService {

    /**
     * 根据名称生成问候语。
     *
     * @param name 名称，为 {@code null} 或空白时返回默认问候语
     * @return 问候语字符串，格式为 "Hello, {name}!"
     */
    String greet(String name);
}