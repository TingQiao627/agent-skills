# hello 模块

## 模块职责

HelloWorld 示例模块，提供基础的问候语生成功能。

## 关键类

| 类名 | 类型 | 说明 |
|------|------|------|
| `HelloWorld` | 服务类 | 提供 `greet(String name)` 方法生成问候语 |
| `HelloWorldTest` | 测试类 | 覆盖正常路径、参数校验、边界值 |

## 依赖关系

- 无外部依赖，纯 Java 标准库实现。

## API 接口列表

| 方法 | 签名 | 说明 |
|------|------|------|
| greet | `String greet(String name)` | 传入名称返回问候语；null/空白时返回默认 `"Hello, World!"` |