# hello 模块

## 模块职责

提供 Hello World 问候语生成服务，支持通过名称参数返回格式化的问候消息。

## 关键类说明

| 类名 | 类型 | 说明 |
|------|------|------|
| `HelloWorldVO` | 视图对象 | 封装问候语和消息内容 |
| `HelloWorldService` | 接口 | 定义问候语生成服务契约 |
| `HelloWorldServiceImpl` | 实现类 | 核心业务逻辑：参数校验、默认值处理、问候语格式化 |
| `HelloWorldController` | 控制器 | 对外暴露服务调用入口，构造器注入 |
| `HelloWorldApplication` | 入口 | 可独立运行的 main 方法 |
| `HelloWorldServiceTest` | 单元测试 | 覆盖正常路径、中文名称、空字符串、null 异常 |

## 依赖关系

- 无外部模块依赖
- 纯 POJO 实现，不依赖任何框架

## API 接口列表

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `greet(String name)` | 生成问候语 | name: 被问候者名称（不可 null） | HelloWorldVO（含 greeting/message） |

### 调用示例

```java
HelloWorldService service = new HelloWorldServiceImpl();
HelloWorldVO result = service.greet("World");
System.out.println(result.getMessage()); // 输出: Hello, World!
```

### 异常说明

- 当 `name` 为 `null` 时，抛出 `IllegalArgumentException("name must not be null")`
- 当 `name` 为空字符串时，使用默认名称 "World"