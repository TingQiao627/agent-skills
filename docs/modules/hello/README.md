# Hello 模块

## 模块职责

提供 Hello World 问候服务，包含 REST 接口和业务逻辑。

## 关键类说明

| 类名 | 类型 | 说明 |
|------|------|------|
| `HelloResponse` | DTO | 问候响应对象，包含 message 和 name 字段 |
| `HelloService` | 接口 | 问候服务接口，定义 `getGreeting(String)` |
| `HelloServiceImpl` | 实现 | 问候服务实现，含参数校验和日志 |
| `HelloController` | 控制器 | REST 控制器，处理问候请求 |
| `HelloServiceImplTest` | 测试 | 单元测试，覆盖正常/边界/异常场景 |

## 依赖关系

无外部模块依赖。内部依赖链：
```
HelloController → HelloService (接口) → HelloServiceImpl
                                  → HelloResponse (DTO)
```

## API 接口列表

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `greet(String name)` | 生成问候语 | name（可选，null 抛异常） | HelloResponse |

## 完成状态

| 阶段 | 状态 |
|------|:----:|
| READ | ✅ |
| TEST | ✅ |
| IMPL | ✅ |
| CHECK | ✅ |
| DOCS | ✅ |