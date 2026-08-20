# HelloWorld 模块

## 模块职责

提供标准 Java Hello World 问候消息输出能力，支持默认消息和自定义名称消息。

## 关键类

| 类名 | 类型 | 说明 |
|------|------|------|
| `HelloWorld` | 主类 | 包含 `main` 入口和 `getMessage` 方法 |
| `HelloWorldTest` | 测试类 | JUnit 5 单元测试，覆盖正常/边界/异常路径 |

## 依赖关系

无外部依赖。纯 Java 标准库实现。

## API 接口

### `getMessage()`

返回默认问候消息 `"Hello, World!"`。

### `getMessage(String name)`

返回指定名称的问候消息 `"Hello, {name}!"`。

- **参数**: `name` — 非 null、非空白的名称字符串
- **返回**: 格式化的问候消息
- **异常**: `IllegalArgumentException` — 当 name 为 null 或空白时抛出

### `main(String[] args)`

程序入口，将问候消息输出到标准输出。

## 测试覆盖

| 测试方法 | 场景 |
|----------|------|
| `should_returnDefaultHelloMessage_when_getMessageCalled` | 默认消息 |
| `should_returnCustomHelloMessage_when_validNameProvided` | 自定义名称 |
| `should_throwException_when_nullNameProvided` | null 参数校验 |
| `should_throwException_when_blankNameProvided` | 空白字符串校验 |