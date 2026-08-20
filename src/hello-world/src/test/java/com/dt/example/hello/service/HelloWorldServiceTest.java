package com.dt.example.hello.service;

import com.dt.example.hello.service.impl.HelloWorldServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link HelloWorldService} 的单元测试。
 *
 * @author hello-world-module
 */
@DisplayName("HelloWorldService 单元测试")
class HelloWorldServiceTest {

    private HelloWorldService sut;

    @BeforeEach
    void setUp() {
        sut = new HelloWorldServiceImpl();
    }

    @Test
    @DisplayName("正常路径：传入有效名称应返回问候语")
    void shouldReturnGreeting_whenValidName() {
        // Arrange
        String name = "World";

        // Act
        String result = sut.greet(name);

        // Assert
        assertThat(result).isEqualTo("Hello, World!");
    }

    @Test
    @DisplayName("边界条件：传入 null 应使用默认名称")
    void shouldReturnGreetingWithDefault_whenNameIsNull() {
        // Arrange
        String name = null;

        // Act
        String result = sut.greet(name);

        // Assert
        assertThat(result).isEqualTo("Hello, World!");
    }

    @Test
    @DisplayName("边界条件：传入空字符串应使用默认名称")
    void shouldReturnGreetingWithDefault_whenNameIsEmpty() {
        // Arrange
        String name = "";

        // Act
        String result = sut.greet(name);

        // Assert
        assertThat(result).isEqualTo("Hello, World!");
    }

    @Test
    @DisplayName("边界条件：传入空白字符串应使用默认名称")
    void shouldReturnGreetingWithDefault_whenNameIsBlank() {
        // Arrange
        String name = "   ";

        // Act
        String result = sut.greet(name);

        // Assert
        assertThat(result).isEqualTo("Hello, World!");
    }

    @Test
    @DisplayName("正常路径：传入中文名称应返回正确问候语")
    void shouldReturnGreeting_whenChineseName() {
        // Arrange
        String name = "数科";

        // Act
        String result = sut.greet(name);

        // Assert
        assertThat(result).isEqualTo("Hello, 数科!");
    }
}