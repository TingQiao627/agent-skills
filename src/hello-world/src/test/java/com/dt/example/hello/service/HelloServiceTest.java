package com.dt.example.hello.service;

import com.dt.example.hello.model.vo.HelloVO;
import com.dt.example.hello.service.impl.HelloServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * HelloService 单元测试。
 *
 * @author dtcoder
 */
@DisplayName("HelloService 单元测试")
class HelloServiceTest {

    private HelloService helloService;

    @BeforeEach
    void setUp() {
        helloService = new HelloServiceImpl();
    }

    // ==================== sayHello 测试 ====================

    @Test
    @DisplayName("正常路径：传入有效名称应返回问候语")
    void should_returnHelloVO_when_validName() {
        // Arrange
        String name = "World";

        // Act
        HelloVO result = helloService.sayHello(name);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).contains("Hello");
        assertThat(result.getMessage()).contains("World");
    }

    @Test
    @DisplayName("正常路径：传入中文名称应返回正确问候语")
    void should_returnHelloVO_when_chineseName() {
        // Arrange
        String name = "世界";

        // Act
        HelloVO result = helloService.sayHello(name);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).contains("Hello");
        assertThat(result.getMessage()).contains("世界");
    }

    @Test
    @DisplayName("参数校验：名称为空字符串时应抛出异常")
    void should_throwException_when_nameIsEmpty() {
        // Arrange
        String name = "";

        // Act & Assert
        assertThatThrownBy(() -> helloService.sayHello(name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");
    }

    @Test
    @DisplayName("参数校验：名称为null时应抛出异常")
    void should_throwException_when_nameIsNull() {
        // Arrange
        String name = null;

        // Act & Assert
        assertThatThrownBy(() -> helloService.sayHello(name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");
    }

    @Test
    @DisplayName("边界值：名称仅含空格时应抛出异常")
    void should_throwException_when_nameIsBlank() {
        // Arrange
        String name = "   ";

        // Act & Assert
        assertThatThrownBy(() -> helloService.sayHello(name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");
    }
}