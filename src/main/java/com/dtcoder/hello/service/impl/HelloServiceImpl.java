package com.dtcoder.hello.service.impl;

import com.dtcoder.hello.service.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello 问候服务实现类
 *
 * @author DTCoder
 * @date 2025/01/20
 */
public class HelloServiceImpl implements HelloService {

    private static final Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);

    /**
     * 根据名称生成问候语
     *
     * @param name 名称，不可为 null
     * @return 问候语字符串；名称为空时返回 "Hello!"
     * @throws IllegalArgumentException 当 name 为 null 时抛出
     */
    @Override
    public String getGreeting(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }

        String trimmedName = name.trim();

        if (trimmedName.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Empty name provided, returning default greeting");
            }
            return "Hello!";
        }

        String greeting = "Hello, " + trimmedName + "!";
        if (logger.isDebugEnabled()) {
            logger.debug("Generated greeting for name: {}", trimmedName);
        }
        return greeting;
    }
}