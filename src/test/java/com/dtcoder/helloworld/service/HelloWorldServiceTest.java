package com.dtcoder.helloworld.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HelloWorldService 单元测试。
 *
 * <p>测试目标：验证业务逻辑层返回正确的 Hello 消息。</p>
 */
class HelloWorldServiceTest {

    private final HelloWorldService sut = new HelloWorldServiceImpl();

    @Test
    @DisplayName("should return hello message when getHelloMessage is called")
    void should_returnHelloMessage_when_getHelloMessage() {
        // Act
        String result = sut.getHelloMessage();

        // Assert
        assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo("Hello, World!");
    }

    @Test
    @DisplayName("should return non-blank message")
    void should_returnNonBlankMessage_when_getHelloMessage() {
        // Act
        String result = sut.getHelloMessage();

        // Assert
        assertThat(result).isNotBlank();
    }
}