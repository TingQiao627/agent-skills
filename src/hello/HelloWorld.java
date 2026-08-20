/**
 * HelloWorld 示例类，提供基本的问候功能。
 *
 * @author DTCoder
 * @date 2025/07/11
 */
public class HelloWorld {

    /** 默认名称 */
    private static final String DEFAULT_NAME = "World";

    /** 问候语模板 */
    private static final String GREETING_TEMPLATE = "Hello, %s!";

    /**
     * 根据传入名称生成问候语，若名称为 null 或空白则使用默认名称。
     *
     * @param name 名称，可为 null
     * @return 问候语字符串
     */
    public String greet(String name) {
        String targetName = (name == null || name.isBlank()) ? DEFAULT_NAME : name.trim();
        return String.format(GREETING_TEMPLATE, targetName);
    }
}