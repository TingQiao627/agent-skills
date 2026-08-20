package com.example.service.impl;

import com.example.service.HelloWorldService;

/**
 * {@link HelloWorldService} 的实现类。
 *
 * <p>提供问候语生成的核心逻辑，遵循数科命名规范（Impl 后缀）。
 *
 * @author dtcoder
 * @since 1.0.0
 */
public class HelloWorldServiceImpl implements HelloWorldService {

    /**
     * 根据指定名称生成问候语。
     *
     * <p>当 name 为 null 或仅含空白字符时，使用 {@value HelloWorldService#DEFAULT_NAME} 作为默认值。
     *
     * @param name 问候对象名称
     * @return 格式化问候语，格式为 "Hello, {name}!"
     */
    @Override
    public String getGreeting(String name) {
        String target = (name == null || name.isBlank()) ? DEFAULT_NAME : name;
        return "Hello, " + target + "!";
    }
}