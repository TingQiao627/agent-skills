package com.example.hello;

import com.example.hello.model.HelloWorldVO;

/**
 * Hello World 业务服务接口
 *
 * @author DTCoder
 * @date 2025/01/16
 */
public interface HelloWorldService {

    /**
     * 生成问候语
     * 根据传入的名称生成格式化的问候消息，若名称为 null 则抛出异常，若为空则使用默认名称 "World"
     *
     * @param name 被问候者名称，不可为 null
     * @return 包含问候信息的视图对象
     * @throws IllegalArgumentException 当 name 为 null 时抛出
     */
    HelloWorldVO greet(String name);
}