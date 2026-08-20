package com.dt.hello.service;

import com.dt.hello.model.vo.HelloWorldResponse;

/**
 * Hello World 业务服务接口。
 *
 * @author dtcoder
 */
public interface HelloWorldService {

    /**
     * 根据名称生成问候消息。
     *
     * @param name 名称，不能为 null
     * @return 问候响应对象
     * @throws IllegalArgumentException 当 name 为 null 时抛出
     */
    HelloWorldResponse greet(String name);
}