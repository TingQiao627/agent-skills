package com.dt.hello.service.impl;

import com.dt.hello.model.vo.HelloWorldResponse;
import com.dt.hello.service.HelloWorldService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * Hello World 业务服务实现。
 *
 * @author dtcoder
 */
public class HelloWorldServiceImpl implements HelloWorldService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelloWorldServiceImpl.class);

    private static final String DEFAULT_NAME = "World";

    @Override
    public HelloWorldResponse greet(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }

        String trimmedName = name.trim();
        String displayName = trimmedName.isEmpty() ? DEFAULT_NAME : trimmedName;

        LOGGER.info("Generating greeting for name: {}", displayName);

        String message = "Hello, " + displayName + "!";
        return new HelloWorldResponse(message, LocalDateTime.now());
    }
}