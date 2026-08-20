package com.dtcoder.hello.service.impl;

import com.dtcoder.hello.service.HelloService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link HelloServiceImpl} 单元测试。
 *
 * @author dtcoder
 */
@DisplayName("HelloServiceImpl 单元测试")
class HelloServiceImplTest {

    private final HelloService helloService = new HelloServiceImpl();

    @Test
    @DisplayName("should return default greeting when name is null")
    void shouldReturnDefaultGreetingWhenNameIsNull() {
        // Arrange
        String expected = "Hello, World!";

        // Act
        String result = helloService.greet(null);

        // Assert
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("should return default greeting when name is blank")
    void shouldReturnDefaultGreetingWhenNameIsBlank() {
        // Arrange
        String expected = "Hello, World!";

        // Act
        String result = helloService.greet("");

        // Assert
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("should return personalized greeting when name is provided")
    void shouldReturnPersonalizedGreetingWhenNameIsProvided() {
        // Arrange
        String name = "DTCoder";
        String expected = "Hello, DTCoder!";

        // Act
        String result = helloService.greet(name);

        // Assert
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("should return personalized greeting when name is whitespace only")
    void shouldReturnDefaultGreetingWhenNameIsWhitespaceOnly() {
        // Arrange
        String expected = "Hello, World!";

        // Act
        String result = helloService.greet("   ");

        // Assert
        assertThat(result).isEqualTo(expected);
    }
}