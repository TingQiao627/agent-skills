package com.dt.example.hello.service.impl;

import com.dt.example.hello.common.constant.HelloWorldConstants;
import com.dt.example.hello.service.HelloWorldService;

/**
 * {@link HelloWorldService} 的实现类。
 *
 * @author hello-world-module
 */
public class HelloWorldServiceImpl implements HelloWorldService {

    @Override
    public String greet(String name) {
        String effectiveName = normalizeName(name);
        return HelloWorldConstants.DEFAULT_GREETING_PREFIX
                + effectiveName
                + HelloWorldConstants.GREETING_SUFFIX;
    }

    /**
     * 规范化名称：若为 {@code null} 或空白则返回默认名称。
     */
    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            return HelloWorldConstants.DEFAULT_NAME;
        }
        return name.trim();
    }
}