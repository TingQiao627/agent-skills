# 项目架构文档

## 模块边界

| 模块 | 类型 | 职责 | 路径 |
|------|------|------|------|
| hello-world | Java | 标准 Hello World 问候消息 | `src/main/java/com/example/HelloWorld.java` |

## 分层架构

本项目为轻量级 Java 示例模块，采用简单的单类设计：

- **主类层**: `HelloWorld` — 业务逻辑 + 程序入口
- **测试层**: `HelloWorldTest` — JUnit 5 单元测试

## 技术栈

- **语言**: Java (JDK 11+)
- **测试框架**: JUnit 5 + AssertJ
- **构建**: 无构建工具（纯 javac 编译）

## 约束

- 遵循 `dtazziboot-java-coding-standards` 编码规范
- 所有公共方法必须有 Javadoc 注释
- 输入参数必须校验 null 和边界条件