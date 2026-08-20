# 架构文档

## 项目概述

Hello World 示例项目，基于 Spring Boot 3.2 + JDK 21。

## 模块列表

| 模块 | 路径 | 说明 |
|------|------|------|
| hello | `src/main/java/com/dt/example/hello/` | Hello World 问候模块 |

## 分层架构

```
com.dt.example.hello
├── api                  # 开放接口层
│   ├── controller       # REST 控制器
│   └── response         # 响应 VO
├── service              # 业务服务层
│   ├── HelloService     # 服务接口
│   └── impl             # 服务实现
├── common               # 公共层
│   └── response         # 统一响应包装
└── HelloWorldApplication  # 启动入口
```

## 技术栈

- JDK 21
- Spring Boot 3.2.0
- JUnit 5 + Mockito + AssertJ (测试)
- Maven

## 约束

- 所有 REST 接口使用 `ApiResponse` 统一包装
- 日志使用 SLF4J + 占位符
- 遵循 dtazziboot-java-coding-standards 编码规范