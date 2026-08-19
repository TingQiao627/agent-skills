package com.example.helloworld;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * HelloWorld 单元测试类
 *
 * @author dtcoder
 * @date 2025/01/20
 */
class HelloWorldTest {

    // ==================== getGreeting 测试 ====================

    @Test
    @DisplayName("调用 getGreeting 应返回 Hello, World!")
    void should_returnHelloWorldGreeting_when_called() {
        // Arrange
        HelloWorld helloWorld = new HelloWorld();

        // Act
        String greeting = helloWorld.getGreeting();

        // Assert
        assertThat(greeting).isNotNull();
        assertThat(greeting).isEqualTo("Hello, World!");
    }

    // ==================== main 测试 ====================

    @Test
    @DisplayName("main 方法应正常执行不抛异常")
    void should_mainMethodExecuteWithoutException() {
        // Arrange & Act & Assert
        assertThatCode(() -> HelloWorld.main(new String[]{}))
                .doesNotThrowAnyException();
    }
}