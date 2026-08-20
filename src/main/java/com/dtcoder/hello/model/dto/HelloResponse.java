package com.dtcoder.hello.model.dto;

/**
 * Hello 响应 DTO
 *
 * @author DTCoder
 * @date 2025/01/20
 */
public class HelloResponse {

    /** 问候消息 */
    private String message;

    /** 请求来源名称 */
    private String name;

    /**
     * 默认构造方法
     */
    public HelloResponse() {
    }

    /**
     * 全参构造方法
     *
     * @param message 问候消息
     * @param name    请求来源名称
     */
    public HelloResponse(String message, String name) {
        this.message = message;
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "HelloResponse{message='" + message + "', name='" + name + "'}";
    }
}