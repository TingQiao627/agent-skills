package com.example.helloworld.service;

/**
 * 问候服务接口。
 *
 * @author helloworld
 */
public interface HelloWorldService {

    /**
     * 根据名称生成问候语。
     *
     * @param name 名称，不能为空
     * @return 问候语字符串
     */
    String greet(String name);
}