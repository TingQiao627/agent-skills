package com.example.hello.service;

import com.example.hello.service.impl.HelloWorldServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link HelloWorldService} 的单元测试。
 *
 * <p>遵循 AAA 模式（Arrange-Act-Assert），覆盖正常路径、边界值与异常场景。</p>
 */
@DisplayName("HelloWorldService 单元测试")
class HelloWorldServiceTest {

    private final HelloWorldService helloWorldService = new HelloWorldServiceImpl();

    // ==================== 正常路径 ====================

    @Test
    @DisplayName("应返回带名称的问候语")
    void shouldReturnGreetingWithName() {
        // Arrange
        String name = "World";

        // Act
        String result = helloWorldService.greet(name);

        // Assert
        assertThat(result)
                .as("问候语应包含传入的名称")
                .isEqualTo("Hello, World!");
    }

    // ==================== 边界值 ====================

    @Test
    @DisplayName("name 为 null 时应返回默认问候语")
    void shouldReturnDefaultGreetingWhenNameIsNull() {
        // Arrange — name 为 null

        // Act
        String result = helloWorldService.greet(null);

        // Assert
        assertThat(result)
                .as("null 输入应返回默认问候语")
                .isEqualTo("Hello, World!");
    }

    @Test
    @DisplayName("name 为空字符串时应返回默认问候语")
    void shouldReturnDefaultGreetingWhenNameIsEmpty() {
        // Arrange
        String name = "";

        // Act
        String result = helloWorldService.greet(name);

        // Assert
        assertThat(result)
                .as("空字符串输入应返回默认问候语")
                .isEqualTo("Hello, World!");
    }

    @Test
    @DisplayName("name 为空白字符串时应返回默认问候语")
    void shouldReturnDefaultGreetingWhenNameIsBlank() {
        // Arrange
        String name = "   ";

        // Act
        String result = helloWorldService.greet(name);

        // Assert
        assertThat(result)
                .as("空白字符串输入应返回默认问候语")
                .isEqualTo("Hello, World!");
    }
}