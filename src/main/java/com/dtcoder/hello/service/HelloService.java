package com.dtcoder.hello.service;

/**
 * Hello 问候服务接口
 *
 * @author DTCoder
 * @date 2025/01/20
 */
public interface HelloService {

    /**
     * 根据名称生成问候语
     *
     * @param name 名称，不可为 null
     * @return 问候语字符串，格式为 "Hello, {name}!"；名称为空时返回 "Hello!"
     * @throws IllegalArgumentException 当 name 为 null 时抛出
     */
    String getGreeting(String name);
}