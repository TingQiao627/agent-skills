package com.dtstack.helloworld.service;

/**
 * Hello World 问候服务接口。
 *
 * @author dtcoder
 */
public interface HelloWorldService {

    /**
     * 根据名称生成问候语。
     * 若名称为 null 或空白字符串，返回默认问候语 "Hello, World!"。
     *
     * @param name 名称，可为 null
     * @return 问候语
     */
    String greet(String name);
}