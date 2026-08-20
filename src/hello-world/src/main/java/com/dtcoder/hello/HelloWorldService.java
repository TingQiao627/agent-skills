package com.dtcoder.hello;

/**
 * Hello World 问候服务接口
 *
 * @author DTCoder
 * @date 2025/07/11
 */
public interface HelloWorldService {

    /**
     * 返回默认问候语 "Hello, World!"
     *
     * @return 默认问候语
     */
    String greet();

    /**
     * 返回个性化问候语
     *
     * @param name 用户名，不能为 null 或空白
     * @return 个性化问候语，格式为 "Hello, {name}!"
     * @throws IllegalArgumentException 当 name 为 null 或空白时抛出
     */
    String greet(String name);
}