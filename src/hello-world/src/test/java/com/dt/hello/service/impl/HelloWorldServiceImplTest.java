package com.dt.hello.service.impl;

import com.dt.hello.model.vo.HelloWorldResponse;
import com.dt.hello.service.HelloWorldService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link HelloWorldServiceImpl} 单元测试。
 *
 * @author dtcoder
 */
@DisplayName("HelloWorldServiceImpl 单元测试")
class HelloWorldServiceImplTest {

    private HelloWorldService helloWorldService;

    @BeforeEach
    void setUp() {
        helloWorldService = new HelloWorldServiceImpl();
    }

    // ──────────────────────────── 正常路径 ────────────────────────────

    @Test
    @DisplayName("正常路径：传入有效名称应返回问候消息")
    void shouldReturnGreeting_whenValidNameGiven() {
        HelloWorldResponse response = helloWorldService.greet("World");

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Hello, World!");
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("正常路径：传入中文名称应返回正确问候")
    void shouldReturnGreeting_whenChineseNameGiven() {
        HelloWorldResponse response = helloWorldService.greet("世界");

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Hello, 世界!");
    }

    // ──────────────────────────── 边界条件 ────────────────────────────

    @Test
    @DisplayName("边界条件：传入空白名称应返回默认问候")
    void shouldReturnDefaultGreeting_whenBlankNameGiven() {
        HelloWorldResponse response = helloWorldService.greet("   ");

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Hello, World!");
    }

    @Test
    @DisplayName("边界条件：传入空字符串应返回默认问候")
    void shouldReturnDefaultGreeting_whenEmptyNameGiven() {
        HelloWorldResponse response = helloWorldService.greet("");

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Hello, World!");
    }

    // ──────────────────────────── 异常处理 ────────────────────────────

    @Test
    @DisplayName("异常处理：传入 null 名称应抛出 IllegalArgumentException")
    void shouldThrowException_whenNullNameGiven() {
        assertThrows(IllegalArgumentException.class,
                () -> helloWorldService.greet(null));
    }
}