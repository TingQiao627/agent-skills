package com.dt.example.hello.service;

import com.dt.example.hello.model.vo.HelloVO;

/**
 * Hello 服务接口。
 *
 * @author dtcoder
 */
public interface HelloService {

    /**
     * 生成问候语。
     *
     * @param name 名称，不能为空
     * @return 包含问候消息的视图对象
     * @throws IllegalArgumentException 当 name 为空或空白时抛出
     */
    HelloVO sayHello(String name);
}