# hello-world 模块

## 模块职责

提供 Hello World 示例程序，演示数科 Java 编码规范下的标准工程结构与核心实践。

## 关键类说明

| 类名 | 类型 | 说明 |
|------|------|------|
| `HelloWorld` | 入口类 | 程序主入口，含 `main` 方法，支持命令行参数 |
| `HelloWorldService` | 接口 | 问候语生成服务契约，定义 `getGreeting(String)` |
| `HelloWorldServiceImpl` | 实现类 | 核心业务逻辑，处理 null/空/空白字符串边界条件 |
| `HelloWorldServiceImplTest` | 测试类 | 4 个测试用例，覆盖正常路径与边界条件 |

## 依赖关系

- 无外部依赖，纯 Java 标准库（JDK 11+）
- 测试依赖 JUnit 5 + AssertJ（仅编译期，未在仓库中声明）

## 包结构

```
com.example
├── HelloWorld.java                  # 主入口
└── service
    ├── HelloWorldService.java       # 服务接口
    └── impl
        ├── HelloWorldServiceImpl.java       # 服务实现
        └── HelloWorldServiceImplTest.java   # 单元测试（位于 src/test/java）
```

## API 接口

| 方法 | 签名 | 说明 |
|------|------|------|
| `getGreeting` | `String getGreeting(String name)` | name 为 null/空/空白时返回 "Hello, World!"，否则返回 "Hello, {name}!" |

## 编码规范遵循

- ✅ 命名规范 (naming.md)
- ✅ 注释规范 (comments.md)
- ✅ 单元测试规范 (unit-testing.md)
- ✅ 接口-实现分离 (Impl 后缀)