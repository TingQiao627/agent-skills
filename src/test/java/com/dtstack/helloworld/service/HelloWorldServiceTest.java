package com.dtstack.helloworld.service;

import com.dtstack.helloworld.service.impl.HelloWorldServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link HelloWorldService} 的单元测试。
 *
 * @author dtcoder
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HelloWorldService 单元测试")
class HelloWorldServiceTest {

    private HelloWorldService helloWorldService;

    @BeforeEach
    void setUp() {
        helloWorldService = new HelloWorldServiceImpl();
    }

    @Test
    @DisplayName("should return default greeting when name is null or empty")
    void should_returnDefaultGreeting_when_nameIsNullOrEmpty() {
        // Arrange
        // Act
        String resultNull = helloWorldService.greet(null);
        String resultEmpty = helloWorldService.greet("");

        // Assert
        assertThat(resultNull).isEqualTo("Hello, World!");
        assertThat(resultEmpty).isEqualTo("Hello, World!");
    }

    @Test
    @DisplayName("should return personalized greeting when name is valid")
    void should_returnPersonalizedGreeting_when_nameIsValid() {
        // Arrange
        String name = "DTCoder";

        // Act
        String result = helloWorldService.greet(name);

        // Assert
        assertThat(result).isEqualTo("Hello, DTCoder!");
    }

    @Test
    @DisplayName("should return greeting when name is blank")
    void should_returnDefaultGreeting_when_nameIsBlank() {
        // Arrange
        String blankName = "   ";

        // Act
        String result = helloWorldService.greet(blankName);

        // Assert
        assertThat(result).isEqualTo("Hello, World!");
    }

    @Test
    @DisplayName("should trim name before greeting")
    void should_trimName_when_nameHasLeadingOrTrailingSpaces() {
        // Arrange
        String nameWithSpaces = "  DTCoder  ";

        // Act
        String result = helloWorldService.greet(nameWithSpaces);

        // Assert
        assertThat(result).isEqualTo("Hello, DTCoder!");
    }
}