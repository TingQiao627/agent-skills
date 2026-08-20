package com.example.service;

/**
 * Hello World 业务服务接口。
 *
 * <p>定义问候语生成的核心契约，遵循数科接口命名规范（方法不加 public 修饰符）。
 *
 * @author dtcoder
 * @since 1.0.0
 */
public interface HelloWorldService {

    /** 默认问候对象 */
    String DEFAULT_NAME = "World";

    /**
     * 根据指定名称生成问候语。
     *
     * @param name 问候对象名称，为 null 或空字符串时使用默认值
     * @return 格式化后的问候语，如 "Hello, World!"
     */
    String getGreeting(String name);
}