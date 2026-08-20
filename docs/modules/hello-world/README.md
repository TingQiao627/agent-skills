# hello-world 模块

## 模块职责

Spring Boot Hello World REST API，返回 "Hello, World!" 消息。

## 关键类

| 类名 | 类型 | 说明 |
|------|------|------|
| `HelloWorldApplication` | 启动类 | Spring Boot 入口 |
| `HelloWorldController` | Controller | REST 端点 `/api/hello` |
| `HelloWorldService` | Service 接口 | 业务接口 |
| `HelloWorldServiceImpl` | Service 实现 | 业务逻辑 |
| `HelloWorldServiceTest` | 测试 | Service 单元测试 |
| `HelloWorldControllerTest` | 测试 | Controller 集成测试 |

## 依赖关系

无外部模块依赖，仅依赖 Spring Boot Starter Web。

## API 接口列表

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/hello` | 返回 "Hello, World!" |