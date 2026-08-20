package com.dt.example.hello.service.impl;

import com.dt.example.hello.service.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 问候服务实现。
 *
 * <p>返回标准的 "Hello World" 问候语。</p>
 */
@Service
public class HelloServiceImpl implements HelloService {

    private static final Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);

    private static final String DEFAULT_GREETING = "Hello World";

    @Override
    public String sayHello() {
        if (logger.isDebugEnabled()) {
            logger.debug("sayHello invoked, returning: {}", DEFAULT_GREETING);
        }
        return DEFAULT_GREETING;
    }
}