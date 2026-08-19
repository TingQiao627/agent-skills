package com.dt.example.hello;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * GreetingServiceImpl 单元测试
 *
 * @author DTCoder
 * @date 2025/01/20
 */
class GreetingServiceImplTest {

    private final GreetingService greetingService = new GreetingServiceImpl();

    // ==================== greet 测试 ====================

    @Test
    void should_returnGreetingWithName_when_nameIsValid() {
        // Arrange
        String name = "World";

        // Act
        String result = greetingService.greet(name);

        // Assert
        assertThat(result).isEqualTo("Hello, World!");
    }

    @Test
    void should_returnDefaultGreeting_when_nameIsEmpty() {
        // Arrange
        String name = "";

        // Act
        String result = greetingService.greet(name);

        // Assert
        assertThat(result).isEqualTo("Hello!");
    }

    @Test
    void should_returnDefaultGreeting_when_nameIsNull() {
        // Arrange
        String name = null;

        // Act
        String result = greetingService.greet(name);

        // Assert
        assertThat(result).isEqualTo("Hello!");
    }
}