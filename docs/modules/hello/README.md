# hello 模块

## 模块职责

提供简单的 Hello World REST API 接口，返回问候信息。

## 关键类说明

| 类名 | 类型 | 说明 |
|------|------|------|
| `HelloApplication` | Spring Boot 启动类 | 应用入口 |
| `HelloController` | REST 控制器 | 暴露 `/api/hello` GET 接口 |
| `HelloService` | 服务接口 | 定义 `sayHello` 方法签名 |
| `HelloServiceImpl` | 服务实现 | 参数校验 + 问候语生成 |
| `HelloVO` | 视图对象 | 封装 `message` 和 `timestamp` |

## 依赖关系

- 仅依赖 Spring Boot Web Starter，无外部模块依赖。

## API 接口列表

| 方法 | 路径 | 参数 | 返回 | 说明 |
|------|------|------|------|------|
| GET | `/api/hello` | `name` (可选, 默认 "World") | `HelloVO` | 返回问候语 |

### 请求示例

```bash
curl http://localhost:8080/api/hello?name=World
```

### 响应示例

```json
{
  "message": "Hello, World!",
  "timestamp": "2026-01-01T12:00:00"
}
```