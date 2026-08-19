package com.dt.example.hello;

/**
 * 问候服务实现类
 *
 * @author DTCoder
 * @date 2025/01/20
 */
public class GreetingServiceImpl implements GreetingService {

    /** 默认问候语 */
    private static final String DEFAULT_GREETING = "Hello!";

    /** 问候语前缀 */
    private static final String GREETING_PREFIX = "Hello, ";

    /** 问候语后缀 */
    private static final String GREETING_SUFFIX = "!";

    @Override
    public String greet(String name) {
        // 名称为空时返回默认问候语
        if (name == null || name.isEmpty()) {
            return DEFAULT_GREETING;
        }
        return GREETING_PREFIX + name + GREETING_SUFFIX;
    }
}