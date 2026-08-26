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

    // ==================== getChineseGreeting 测试 ====================

    @Test
    @DisplayName("调用 getChineseGreeting 应返回 你好，世界！")
    void should_returnChineseGreeting_when_called() {
        // Arrange
        HelloWorld helloWorld = new HelloWorld();

        // Act
        String greeting = helloWorld.getChineseGreeting();

        // Assert
        assertThat(greeting).isNotNull();
        assertThat(greeting).isEqualTo("你好，世界！");
    }

    // ==================== getGreeting(name) 测试 ====================

    @Test
    @DisplayName("传入名称调用 getGreeting 应返回包含该名称的问候语")
    void should_returnNamedGreeting_when_nameProvided() {
        // Arrange
        HelloWorld helloWorld = new HelloWorld();

        // Act
        String greeting = helloWorld.getGreeting("Alice");

        // Assert
        assertThat(greeting).isEqualTo("Hello, Alice!");
    }

    @Test
    @DisplayName("传入空名称调用 getGreeting 应默认问候 World")
    void should_returnDefaultGreeting_when_nameBlank() {
        // Arrange
        HelloWorld helloWorld = new HelloWorld();

        // Act
        String greeting = helloWorld.getGreeting("   ");

        // Assert
        assertThat(greeting).isEqualTo("Hello, World!");
    }

    // ==================== getChineseGreeting(name) 测试 ====================

    @Test
    @DisplayName("传入名称调用 getChineseGreeting 应返回包含该名称的中文问候语")
    void should_returnNamedChineseGreeting_when_nameProvided() {
        // Arrange
        HelloWorld helloWorld = new HelloWorld();

        // Act
        String greeting = helloWorld.getChineseGreeting("小明");

        // Assert
        assertThat(greeting).isEqualTo("你好，小明！");
    }

    @Test
    @DisplayName("传入空名称调用 getChineseGreeting 应默认问候 世界")
    void should_returnDefaultChineseGreeting_when_nameBlank() {
        // Arrange
        HelloWorld helloWorld = new HelloWorld();

        // Act
        String greeting = helloWorld.getChineseGreeting(null);

        // Assert
        assertThat(greeting).isEqualTo("你好，世界！");
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