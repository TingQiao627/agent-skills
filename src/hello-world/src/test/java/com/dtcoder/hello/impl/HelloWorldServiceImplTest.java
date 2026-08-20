package com.dtcoder.hello.impl;

import com.dtcoder.hello.HelloWorldService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * HelloWorldServiceImpl 单元测试
 *
 * @author DTCoder
 * @date 2025/07/11
 */
@DisplayName("HelloWorldServiceImpl 单元测试")
class HelloWorldServiceImplTest {

    private HelloWorldService helloWorldService;

    @BeforeEach
    void setUp() {
        helloWorldService = new HelloWorldServiceImpl();
    }

    // ==================== greet() 测试 ====================

    @Test
    @DisplayName("无参调用应返回默认问候语")
    void should_returnDefaultGreeting_when_greetWithoutName() {
        // Arrange
        // Act
        String result = helloWorldService.greet();

        // Assert
        assertThat(result).isEqualTo("Hello, World!");
    }

    // ==================== greet(String) 测试 ====================

    @Test
    @DisplayName("传入有效名称应返回个性化问候语")
    void should_returnPersonalizedGreeting_when_greetWithValidName() {
        // Arrange
        String name = "DTCoder";

        // Act
        String result = helloWorldService.greet(name);

        // Assert
        assertThat(result).isEqualTo("Hello, DTCoder!");
    }

    @Test
    @DisplayName("传入 null 名称应抛出 IllegalArgumentException")
    void should_throwException_when_nameIsNull() {
        // Arrange
        // Act & Assert
        assertThatThrownBy(() -> helloWorldService.greet(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name must not be null or blank");
    }

    @Test
    @DisplayName("传入空白名称应抛出 IllegalArgumentException")
    void should_throwException_when_nameIsBlank() {
        // Arrange
        // Act & Assert
        assertThatThrownBy(() -> helloWorldService.greet("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name must not be null or blank");
    }

    @Test
    @DisplayName("传入空字符串应抛出 IllegalArgumentException")
    void should_throwException_when_nameIsEmpty() {
        // Arrange
        // Act & Assert
        assertThatThrownBy(() -> helloWorldService.greet(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name must not be null or blank");
    }
}