package com.example.helloworld.service;

import com.example.helloworld.service.impl.HelloWorldServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HelloWorldService 单元测试
 *
 * @author dtcoder
 * @date 2025/07/10
 */
@DisplayName("HelloWorldService 单元测试")
class HelloWorldServiceTest {

    private final HelloWorldService helloWorldService = new HelloWorldServiceImpl();

    /**
     * 测试正常路径：获取问候消息
     */
    @Test
    @DisplayName("正常路径：应返回 Hello, World! 消息")
    void should_returnHelloWorldMessage_when_getMessageCalled() {
        // Arrange
        String expected = "Hello, World!";

        // Act
        String actual = helloWorldService.getMessage();

        // Assert
        assertThat(actual).isEqualTo(expected);
    }

    /**
     * 测试边界条件：返回的消息不应为空
     */
    @Test
    @DisplayName("边界条件：返回的消息不应为空")
    void should_returnNonEmptyMessage_when_getMessageCalled() {
        // Act
        String message = helloWorldService.getMessage();

        // Assert
        assertThat(message).isNotNull().isNotBlank();
    }
}