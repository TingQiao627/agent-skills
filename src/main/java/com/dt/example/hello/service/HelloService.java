package com.dt.example.hello.service;

/**
 * 问候服务接口。
 *
 * <p>提供与问候相关的业务能力。</p>
 */
public interface HelloService {

    /**
     * 返回问候语。
     *
     * @return 问候语字符串，不会为 null
     */
    String sayHello();
}