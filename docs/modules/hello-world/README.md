# hello-world 模块

## 模块职责

提供 Hello World 问候服务，作为 Java 编码规范的示例模块。

## 关键类

| 类 | 类型 | 说明 |
|----|------|------|
| `GreetingService` | 接口 | 问候服务接口，定义 `greet(name)` 方法 |
| `GreetingServiceImpl` | 实现类 | 问候服务实现，处理空名称边界情况 |
| `HelloWorldApplication` | 入口 | 应用主入口，演示服务调用 |

## 依赖关系

- 无外部依赖，纯 Java SE 实现
- 测试依赖：JUnit 5 + AssertJ

## 包结构

```
com.dt.example.hello
├── GreetingService.java          # 服务接口
├── GreetingServiceImpl.java      # 服务实现
└── HelloWorldApplication.java    # 应用入口
```