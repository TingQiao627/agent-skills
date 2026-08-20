package com.dt.example.hello.model.vo;

import java.time.LocalDateTime;

/**
 * Hello 视图对象。
 *
 * @author dtcoder
 */
public class HelloVO {

    /** 问候消息 */
    private String message;

    /** 响应时间戳 */
    private LocalDateTime timestamp;

    /**
     * 默认构造方法。
     */
    public HelloVO() {
    }

    /**
     * 带参构造方法。
     *
     * @param message   问候消息
     * @param timestamp 响应时间戳
     */
    public HelloVO(String message, LocalDateTime timestamp) {
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