package com.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * HelloWorld 单元测试。
 *
 * @author DTCoder
 */
class HelloWorldTest {

    private final HelloWorld helloWorld = new HelloWorld();

    @Test
    @DisplayName("should return default greeting when no name provided")
    void should_returnDefaultGreeting_when_noNameProvided() {
        String result = helloWorld.greet();
        assertNotNull(result, "greeting should not be null");
        assertEquals("Hello, World!", result, "default greeting should be 'Hello, World!'");
    }

    @Test
    @DisplayName("should return personalized greeting when name provided")
    void should_returnPersonalizedGreeting_when_nameProvided() {
        String result = helloWorld.greet("DTCoder");
        assertNotNull(result, "greeting should not be null");
        assertEquals("Hello, DTCoder!", result, "personalized greeting should include the name");
    }

    @Test
    @DisplayName("should handle null name gracefully")
    void should_handleNullName_when_nameIsNull() {
        String result = helloWorld.greet(null);
        assertNotNull(result, "greeting should not be null for null input");
        assertEquals("Hello, World!", result, "null name should fallback to default greeting");
    }

    @Test
    @DisplayName("should handle empty name gracefully")
    void should_handleEmptyName_when_nameIsEmpty() {
        String result = helloWorld.greet("");
        assertNotNull(result, "greeting should not be null for empty input");
        assertEquals("Hello, World!", result, "empty name should fallback to default greeting");
    }
}