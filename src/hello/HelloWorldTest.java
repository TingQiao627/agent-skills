import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HelloWorld 单元测试
 *
 * @author DTCoder
 * @date 2025/07/11
 */
class HelloWorldTest {

    private final HelloWorld helloWorld = new HelloWorld();

    /**
     * 测试方法列表：
     * | 方法 | 测试场景 | 状态 |
     * |------|----------|:----:|
     * | shouldReturnGreeting_whenValidName | 正常路径 | ✅ |
     * | shouldReturnDefaultGreeting_whenNameNull | 参数为 null | ✅ |
     * | shouldReturnDefaultGreeting_whenNameEmpty | 参数为空串 | ✅ |
     * | shouldReturnDefaultGreeting_whenNameBlank | 参数为空白 | ✅ |
     */

    /**
     * 正常路径：传入有效名称，返回个性化问候语
     */
    @Test
    @DisplayName("传入有效名称应返回个性化问候")
    void shouldReturnGreeting_whenValidName() {
        // Arrange
        String name = "World";

        // Act
        String result = helloWorld.greet(name);

        // Assert
        assertEquals("Hello, World!", result);
    }

    /**
     * 参数校验：传入 null，返回默认问候语
     */
    @Test
    @DisplayName("传入 null 应返回默认问候")
    void shouldReturnDefaultGreeting_whenNameNull() {
        // Arrange
        String name = null;

        // Act
        String result = helloWorld.greet(name);

        // Assert
        assertEquals("Hello, World!", result);
    }

    /**
     * 边界值：传入空字符串，返回默认问候语
     */
    @Test
    @DisplayName("传入空字符串应返回默认问候")
    void shouldReturnDefaultGreeting_whenNameEmpty() {
        // Arrange
        String name = "";

        // Act
        String result = helloWorld.greet(name);

        // Assert
        assertEquals("Hello, World!", result);
    }

    /**
     * 边界值：传入纯空白字符串，返回默认问候语
     */
    @Test
    @DisplayName("传入纯空白字符串应返回默认问候")
    void shouldReturnDefaultGreeting_whenNameBlank() {
        // Arrange
        String name = "   ";

        // Act
        String result = helloWorld.greet(name);

        // Assert
        assertEquals("Hello, World!", result);
    }
}