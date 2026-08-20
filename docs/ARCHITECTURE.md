# 架构文档

## 项目概述

Hello World 示例项目，基于 Spring Boot 3.2 + JDK 21 构建。

## 技术栈

| 组件 | 版本 |
|------|------|
| Java | 21 |
| Spring Boot | 3.2.0 |
| 测试框架 | JUnit 5 + Mockito + AssertJ |

## 模块列表

| 模块 | 说明 |
|------|------|
| [hello](modules/hello/README.md) | Hello World REST API 模块 |

## 分层架构

```
com.dtcoder.hello
├── controller          # Web 层 - REST 控制器
├── service             # Service 层 - 业务接口
│   └── impl            # Service 实现
```

## 分层调用关系

Controller → Service → 返回问候语

## 约束

- 遵循 dtazziboot-java-coding-standards 编码规范
- 包名小写，类名大驼峰，方法名小驼峰
- 接口方法不加 public abstract 修饰符