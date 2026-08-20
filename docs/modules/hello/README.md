# Hello 模块

## 模块职责

提供 REST API 返回 "Hello World" 问候语，作为项目的基础示例模块。

## 关键类说明

| 类名 | 层次 | 职责 |
|------|------|------|
| `HelloWorldApplication` | 启动入口 | Spring Boot 应用启动类 |
| `HelloController` | API 层 | 暴露 GET /hello 端点 |
| `HelloService` | Service 接口 | 问候业务接口 |
| `HelloServiceImpl` | Service 实现 | 问候业务实现，返回 "Hello World" |
| `HelloVO` | 视图对象 | 封装问候语返回前端 |
| `ApiResponse` | 通用响应 | 统一 API 响应包装 |

## 依赖关系

无外部模块依赖，独立模块。

## API 接口列表

| 方法 | 路径 | 说明 | 请求参数 | 响应 |
|------|------|------|----------|------|
| GET | `/hello` | 获取问候语 | 无 | `ApiResponse<HelloVO>` |

### 响应示例

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "message": "Hello World"
    }
}
```