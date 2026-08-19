# 架构文档

## 项目概述

数科业务 Java 技能演示仓库，包含 Java 编码规范示例模块。

## 模块列表

| 模块 | 路径 | 说明 |
|------|------|------|
| hello-world | `src/main/java/com/example/helloworld/` | Hello World 示例，演示 Java 编码规范 |

## 分层架构

```
src/
├── main/java/com/example/helloworld/   # 业务代码
└── test/java/com/example/helloworld/   # 单元测试
```

## 技术栈

- Java SE (JDK 21 LTS)
- JUnit 5
- AssertJ

## 约束与规范

- 遵循 `dtazziboot-java-coding-standards` 编码规范
- 所有类必须含 Javadoc 注释，含 @author 和 @date
- 测试采用 TDD 模式，AAA 结构