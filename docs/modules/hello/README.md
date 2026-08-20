# hello 模块

## 模块职责

提供 Hello World REST API，根据可选名称参数返回个性化问候语。

## 关键类

| 类 | 类型 | 说明 |
|----|------|------|
| `HelloApplication` | 启动类 | Spring Boot 应用入口 |
| `HelloController` | 控制器 | 暴露 `GET /api/hello` 端点 |
| `HelloService` | 接口 | 问候服务契约 |
| `HelloServiceImpl` | 实现 | 问候业务逻辑 |

## 依赖关系

无外部模块依赖。仅依赖 Spring Boot Starter Web。

## API 接口列表

### GET /api/hello

返回问候语。

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| name | String | 否 | 名称，为空时返回 "Hello, World!" |

**响应示例**：

```
GET /api/hello        → "Hello, World!"
GET /api/hello?name=DTCoder → "Hello, DTCoder!"
```

## 测试覆盖

| 测试类 | 测试方法数 | 覆盖场景 |
|--------|:----------:|----------|
| `HelloServiceImplTest` | 4 | 正常路径、null/空/空白参数 |
| `HelloControllerTest` | 3 | 无参数、有参数、空参数 |