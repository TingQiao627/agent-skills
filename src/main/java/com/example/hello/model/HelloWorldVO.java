package com.example.hello.model;

/**
 * Hello World 视图对象
 *
 * @author DTCoder
 * @date 2025/01/16
 */
public class HelloWorldVO {

    /** 问候语 */
    private String greeting;

    /** 消息内容 */
    private String message;

    /**
     * 默认构造器
     */
    public HelloWorldVO() {
    }

    /**
     * 带参构造器
     *
     * @param greeting 问候语
     * @param message  消息内容
     */
    public HelloWorldVO(String greeting, String message) {
        this.greeting = greeting;
        this.message = message;
    }

    public String getGreeting() {
        return greeting;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}