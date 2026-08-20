package com.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HelloWorld 单元测试类。
 *
 * @author DTCoder
 */
class HelloWorldTest {

    @Test
    @DisplayName("should return default hello message when getMessage() is called")
    void should_returnDefaultHelloMessage_when_getMessageCalled() {
        // Arrange
        HelloWorld helloWorld = new HelloWorld();

        // Act
        String message = helloWorld.getMessage();

        // Assert
        assertEquals("Hello, World!", message);
    }

    @Test
    @DisplayName("should return custom hello message when getMessage(name) is called with valid name")
    void should_returnCustomHelloMessage_when_validNameProvided() {
        // Arrange
        HelloWorld helloWorld = new HelloWorld();
        String name = "DTCoder";

        // Act
        String message = helloWorld.getMessage(name);

        // Assert
        assertEquals("Hello, DTCoder!", message);
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when getMessage(name) is called with null")
    void should_throwException_when_nullNameProvided() {
        // Arrange
        HelloWorld helloWorld = new HelloWorld();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> helloWorld.getMessage(null));
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when getMessage(name) is called with blank string")
    void should_throwException_when_blankNameProvided() {
        // Arrange
        HelloWorld helloWorld = new HelloWorld();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> helloWorld.getMessage("   "));
    }
}