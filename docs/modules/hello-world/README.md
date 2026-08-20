# hello-world 模块

## 模块职责

Hello World 示例模块，提供标准问候服务。演示 Java 分层架构、命名规范、单元测试等最佳实践。

## 关键类

| 类 | 类型 | 说明 |
|----|------|------|
| `HelloWorldApplication` | 入口 | 应用主入口，解析命令行参数 |
| `HelloWorldService` | 接口 | 问候服务接口，定义 `greet(String)` |
| `HelloWorldServiceImpl` | 实现 | 服务实现，处理 null/空白名称回退 |
| `HelloWorldConstants` | 常量 | 默认问候前缀、默认名称、后缀 |

## 依赖关系

无外部模块依赖。仅依赖 JDK 标准库。

## API 接口列表

| 方法 | 说明 |
|------|------|
| `String greet(String name)` | 返回格式化的问候语；name 为 null/空白时使用默认名称 "World" |