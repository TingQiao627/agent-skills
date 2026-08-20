# ARCHITECTURE.md

## 项目概述

本仓库为 AI 编码技能（Skills）集合，扩展 Claude.ai 和 Claude Code 的能力。同时包含示例 Java 模块。

## 模块列表

| 模块 | 路径 | 说明 |
|------|------|------|
| hello-world | `src/main/java/com/dtcoder/helloworld/` | Hello World Spring Boot 示例应用 |

## 技术栈

- **Java 21** + Spring Boot 3.2.x
- Maven 构建
- JUnit 5 + Mockito + AssertJ 测试

## 分层架构

```
Controller → Service (接口) → ServiceImpl (实现)
```

## 约束

- 接口与实现分离（Impl 后缀）
- 包名统一小写
- 测试代码包路径与主代码一致