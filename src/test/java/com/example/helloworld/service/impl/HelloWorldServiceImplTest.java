package com.example.helloworld.service.impl;

import com.example.helloworld.service.HelloWorldService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link HelloWorldServiceImpl} 的单元测试。
 */
@DisplayName("HelloWorldServiceImpl 单元测试")
class HelloWorldServiceImplTest {

    private HelloWorldService helloWorldService;

    @BeforeEach
    void setUp() {
        helloWorldService = new HelloWorldServiceImpl();
    }

    // ==================== greet 测试 ====================

    @Test
    @DisplayName("正常路径：传入有效名称应返回问候语")
    void should_returnGreeting_when_validName() {
        // Arrange
        String name = "World";

        // Act
        String result = helloWorldService.greet(name);

        // Assert
        assertThat(result).isEqualTo("Hello, World!");
    }

    @Test
    @DisplayName("正常路径：传入中文名称应返回问候语")
    void should_returnGreeting_when_chineseName() {
        // Arrange
        String name = "数科";

        // Act
        String result = helloWorldService.greet(name);

        // Assert
        assertThat(result).isEqualTo("Hello, 数科!");
    }

    @Test
    @DisplayName("参数校验：传入 null 应抛出异常")
    void should_throwException_when_nameIsNull() {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> helloWorldService.greet(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name must not be null");
    }

    @Test
    @DisplayName("参数校验：传入空字符串应抛出异常")
    void should_throwException_when_nameIsEmpty() {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> helloWorldService.greet(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name must not be blank");
    }

    @Test
    @DisplayName("参数校验：传入纯空白字符串应抛出异常")
    void should_throwException_when_nameIsBlank() {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> helloWorldService.greet("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name must not be blank");
    }
}