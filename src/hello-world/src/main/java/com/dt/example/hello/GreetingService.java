package com.dt.example.hello;

/**
 * 问候服务接口
 *
 * @author DTCoder
 * @date 2025/01/20
 */
public interface GreetingService {

    /**
     * 根据名称生成问候语
     *
     * @param name 名称，为空时返回默认问候语
     * @return 问候语字符串
     */
    String greet(String name);
}