package com.example.service.impl;

import com.example.service.HelloWorldService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link HelloWorldServiceImpl} 单元测试。
 *
 * <p>遵循 AAA (Arrange-Act-Assert) 模式，覆盖正常路径、null 输入、空字符串边界条件。
 *
 * @author dtcoder
 * @since 1.0.0
 */
@DisplayName("HelloWorldServiceImpl 单元测试")
class HelloWorldServiceImplTest {

    private HelloWorldService helloWorldService;

    @BeforeEach
    void setUp() {
        helloWorldService = new HelloWorldServiceImpl();
    }

    // ==================== getGreeting 测试 ====================

    @Test
    @DisplayName("正常路径：传入有效名称应返回含该名称的问候语")
    void should_returnGreetingWithName_when_validNameProvided() {
        // Arrange
        String name = "DTCoder";

        // Act
        String result = helloWorldService.getGreeting(name);

        // Assert
        assertThat(result).isEqualTo("Hello, DTCoder!");
    }

    @Test
    @DisplayName("边界条件：传入 null 应返回默认问候语")
    void should_returnDefaultGreeting_when_nameIsNull() {
        // Arrange
        String name = null;

        // Act
        String result = helloWorldService.getGreeting(name);

        // Assert
        assertThat(result).isEqualTo("Hello, World!");
    }

    @Test
    @DisplayName("边界条件：传入空字符串应返回默认问候语")
    void should_returnDefaultGreeting_when_nameIsEmpty() {
        // Arrange
        String name = "";

        // Act
        String result = helloWorldService.getGreeting(name);

        // Assert
        assertThat(result).isEqualTo("Hello, World!");
    }

    @Test
    @DisplayName("边界条件：传入空白字符串应返回默认问候语")
    void should_returnDefaultGreeting_when_nameIsBlank() {
        // Arrange
        String name = "   ";

        // Act
        String result = helloWorldService.getGreeting(name);

        // Assert
        assertThat(result).isEqualTo("Hello, World!");
    }
}