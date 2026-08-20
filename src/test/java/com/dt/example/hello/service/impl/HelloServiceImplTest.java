package com.dt.example.hello.service.impl;

import com.dt.example.hello.service.HelloService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link HelloServiceImpl} 单元测试。
 *
 * <p>遵循 AAA 模式（Arrange-Act-Assert），覆盖正常路径与边界条件。</p>
 */
@DisplayName("HelloServiceImpl 单元测试")
class HelloServiceImplTest {

    private final HelloService helloService = new HelloServiceImpl();

    @Test
    @DisplayName("should return greeting message when called")
    void shouldReturnGreetingMessageWhenCalled() {
        // Act
        String result = helloService.sayHello();

        // Assert
        assertThat(result)
                .isNotNull()
                .isNotBlank()
                .contains("Hello");
    }

    @Test
    @DisplayName("should return consistent result for multiple calls")
    void shouldReturnConsistentResultForMultipleCalls() {
        // Act
        String first = helloService.sayHello();
        String second = helloService.sayHello();

        // Assert
        assertThat(first).isEqualTo(second);
    }
}