package com.example.hello;

import com.example.hello.model.HelloWorldVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * HelloWorldService 单元测试
 *
 * @author DTCoder
 * @date 2025/01/16
 */
@DisplayName("HelloWorldService 单元测试")
class HelloWorldServiceTest {

    private final HelloWorldService helloWorldService = new HelloWorldServiceImpl();

    @Test
    @DisplayName("正常路径：传入有效名称，返回问候语")
    void shouldReturnGreetingWhenValidNameProvided() {
        // Arrange
        String name = "World";

        // Act
        HelloWorldVO result = helloWorldService.greet(name);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Hello, World!");
        assertThat(result.getGreeting()).isEqualTo("Hello, World!");
    }

    @Test
    @DisplayName("正常路径：传入中文名称，返回中文问候语")
    void shouldReturnGreetingWhenChineseNameProvided() {
        // Arrange
        String name = "世界";

        // Act
        HelloWorldVO result = helloWorldService.greet(name);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Hello, 世界!");
    }

    @Test
    @DisplayName("边界条件：传入空字符串")
    void shouldReturnDefaultGreetingWhenEmptyNameProvided() {
        // Arrange
        String name = "";

        // Act
        HelloWorldVO result = helloWorldService.greet(name);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Hello, World!");
    }

    @Test
    @DisplayName("异常路径：传入 null 参数，抛出异常")
    void shouldThrowExceptionWhenNameIsNull() {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> helloWorldService.greet(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name must not be null");
    }
}