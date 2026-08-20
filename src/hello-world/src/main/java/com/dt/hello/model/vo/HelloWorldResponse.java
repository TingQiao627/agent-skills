package com.dt.hello.model.vo;

import java.time.LocalDateTime;

/**
 * Hello World 响应对象。
 *
 * @author dtcoder
 */
public class HelloWorldResponse {

    /** 问候消息 */
    private String message;

    /** 响应时间戳 */
    private LocalDateTime timestamp;

    public HelloWorldResponse() {
    }

    public HelloWorldResponse(String message, LocalDateTime timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}