# hello-world 模块

## 模块职责

提供 Hello World 问候功能，支持默认问候和个性化问候。

## 关键类

| 类名 | 路径 | 说明 |
|------|------|------|
| HelloWorld | `src/main/java/com/example/HelloWorld.java` | 问候服务类 |
| HelloWorldTest | `src/test/java/com/example/HelloWorldTest.java` | 单元测试类 |

## 依赖关系

- 无外部依赖，纯 Java 标准库（JDK 11+）
- 测试依赖：JUnit 5

## API 接口列表

| 方法 | 签名 | 说明 |
|------|------|------|
| greet | `String greet()` | 返回默认问候语 "Hello, World!" |
| greet | `String greet(String name)` | 返回个性化问候语 "Hello, {name}!"，name 为 null 或空白时抛 IllegalArgumentException |

## 使用示例

```java
HelloWorld helloWorld = new HelloWorld();

// 默认问候
String greeting = helloWorld.greet();  // "Hello, World!"

// 个性化问候
String personalized = helloWorld.greet("Alice");  // "Hello, Alice!"
```