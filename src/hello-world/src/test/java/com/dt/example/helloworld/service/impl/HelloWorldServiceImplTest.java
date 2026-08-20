package com.dt.example.helloworld.service.impl;

import com.dt.example.helloworld.service.HelloWorldService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * HelloWorldServiceImpl 单元测试
 *
 * <p>覆盖正常路径、参数校验、边界值等场景，遵循 AAA（Arrange-Act-Assert）模式。
 */
@DisplayName("HelloWorldServiceImpl 单元测试")
class HelloWorldServiceImplTest {

    private HelloWorldService helloWorldService;

    @BeforeEach
    void setUp() {
        helloWorldService = new HelloWorldServiceImpl();
    }

    @Test
    @DisplayName("正常返回默认欢迎消息")
    void shouldReturnDefaultGreeting_whenNoNameProvided() {
        // Arrange
        String expected = "Hello, World!";

        // Act
        String result = helloWorldService.greet(null);

        // Assert
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("正常返回带名称的欢迎消息")
    void shouldReturnPersonalizedGreeting_whenNameProvided() {
        // Arrange
        String name = "DTCoder";
        String expected = "Hello, DTCoder!";

        // Act
        String result = helloWorldService.greet(name);

        // Assert
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("空字符串应返回默认欢迎消息")
    void shouldReturnDefaultGreeting_whenNameIsEmpty() {
        // Arrange
        String expected = "Hello, World!";

        // Act
        String result = helloWorldService.greet("");

        // Assert
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("空白字符串应返回默认欢迎消息")
    void shouldReturnDefaultGreeting_whenNameIsBlank() {
        // Arrange
        String expected = "Hello, World!";

        // Act
        String result = helloWorldService.greet("   ");

        // Assert
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("超长名称应抛出异常")
    void shouldThrowException_whenNameExceedsMaxLength() {
        // Arrange
        String tooLongName = "A".repeat(101);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> helloWorldService.greet(tooLongName));
    }
}