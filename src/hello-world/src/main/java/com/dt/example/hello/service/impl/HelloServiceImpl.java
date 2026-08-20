package com.dt.example.hello.service.impl;

import com.dt.example.hello.model.vo.HelloVO;
import com.dt.example.hello.service.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Hello 服务实现类。
 *
 * @author dtcoder
 */
@Service
public class HelloServiceImpl implements HelloService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelloServiceImpl.class);

    /**
     * 生成问候语。
     *
     * @param name 名称，不能为空
     * @return 包含问候消息的视图对象
     * @throws IllegalArgumentException 当 name 为空或空白时抛出
     */
    @Override
    public HelloVO sayHello(String name) {
        if (name == null || name.isBlank()) {
            LOGGER.warn("sayHello 参数校验失败：name 为空或空白");
            throw new IllegalArgumentException("name 不能为空或空白");
        }

        String message = "Hello, " + name + "!";
        LOGGER.info("生成问候语成功：{}", message);

        return new HelloVO(message, LocalDateTime.now());
    }
}