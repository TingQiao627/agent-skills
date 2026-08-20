package com.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * HelloWorld 单元测试类。
 *
 * <p>遵循 AAA（Arrange-Act-Assert）模式，覆盖正常路径、边界值和异常场景。</p>
 *
 * @author dtcoder
 */
@DisplayName("HelloWorld 单元测试")
class HelloWorldTest {

    private static final String DEFAULT_GREETING = "Hello, World!";
    private static final String TEST_NAME = "Alice";

    // ==================== greet() 无参方法测试 ====================

    @Test
    @DisplayName("should return default greeting when no name provided")
    void should_returnDefaultGreeting_when_noNameProvided() {
        // Arrange
        HelloWorld helloWorld = new HelloWorld();

        // Act
        String result = helloWorld.greet();

        // Assert
        assertEquals(DEFAULT_GREETING, result);
    }

    // ==================== greet(String) 有参方法测试 ====================

    @Test
    @DisplayName("should return personalized greeting when name provided")
    void should_returnPersonalizedGreeting_when_nameProvided() {
        // Arrange
        HelloWorld helloWorld = new HelloWorld();

        // Act
        String result = helloWorld.greet(TEST_NAME);

        // Assert
        assertEquals("Hello, Alice!", result);
    }

    @Test
    @DisplayName("should throw exception when name is null")
    void should_throwException_when_nameIsNull() {
        // Arrange
        HelloWorld helloWorld = new HelloWorld();

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> helloWorld.greet(null));
    }

    @Test
    @DisplayName("should throw exception when name is blank")
    void should_throwException_when_nameIsBlank() {
        // Arrange
        HelloWorld helloWorld = new HelloWorld();

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> helloWorld.greet(""));
    }

    @Test
    @DisplayName("should throw exception when name is whitespace only")
    void should_throwException_when_nameIsWhitespaceOnly() {
        // Arrange
        HelloWorld helloWorld = new HelloWorld();

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> helloWorld.greet("   "));
    }
}