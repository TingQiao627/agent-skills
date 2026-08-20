package com.dtcoder.hello.service;

/**
 * 问候服务接口。
 *
 * @author dtcoder
 */
public interface HelloService {

    /**
     * 根据名称生成问候语。
     *
     * @param name 名称，可为 null 或空白
     * @return 问候语，当 name 为 null 或空白时返回 "Hello, World!"
     */
    String greet(String name);
}