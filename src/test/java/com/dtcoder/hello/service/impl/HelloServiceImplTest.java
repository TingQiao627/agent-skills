package com.dtcoder.hello.service.impl;

import com.dtcoder.hello.service.HelloService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * HelloService 实现类单元测试
 *
 * @author DTCoder
 * @date 2025/01/20
 */
@DisplayName("HelloServiceImpl 单元测试")
class HelloServiceImplTest {

    private HelloService helloService;

    @BeforeEach
    void setUp() {
        helloService = new HelloServiceImpl();
    }

    // ==================== getGreeting 测试 ====================

    @Test
    @DisplayName("正常路径：传入有效名称应返回问候语")
    void should_returnGreeting_when_validName() {
        // Arrange
        String name = "World";

        // Act
        String result = helloService.getGreeting(name);

        // Assert
        assertThat(result)
                .as("问候语应包含传入的名称")
                .isEqualTo("Hello, World!");
    }

    @Test
    @DisplayName("正常路径：传入中文名称应返回正确问候语")
    void should_returnGreeting_when_chineseName() {
        // Arrange
        String name = "世界";

        // Act
        String result = helloService.getGreeting(name);

        // Assert
        assertThat(result)
                .as("问候语应包含中文名称")
                .isEqualTo("Hello, 世界!");
    }

    @Test
    @DisplayName("边界值：传入空字符串应返回默认问候语")
    void should_returnDefaultGreeting_when_emptyName() {
        // Arrange
        String name = "";

        // Act
        String result = helloService.getGreeting(name);

        // Assert
        assertThat(result)
                .as("空名称时应返回默认问候语")
                .isEqualTo("Hello!");
    }

    @Test
    @DisplayName("异常路径：传入 null 应抛出 IllegalArgumentException")
    void should_throwException_when_nameIsNull() {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> helloService.getGreeting(null))
                .as("null 参数应抛出 IllegalArgumentException")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name must not be null");
    }

    @Test
    @DisplayName("边界值：传入仅含空格的名称应去掉首尾空格后返回问候语")
    void should_trimWhitespace_when_nameHasSpaces() {
        // Arrange
        String name = "  World  ";

        // Act
        String result = helloService.getGreeting(name);

        // Assert
        assertThat(result)
                .as("应去掉首尾空格")
                .isEqualTo("Hello, World!");
    }
}