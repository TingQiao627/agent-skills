# 架构文档

## 模块列表

| 模块 | 路径 | 说明 |
|------|------|------|
| hello-world | `src/hello-world/` | Hello World 示例模块，演示标准 Java 分层架构 |

## 分层架构

```
com.dt.example.hello
├── HelloWorldApplication    # 应用入口
├── service                  # 服务层
│   ├── HelloWorldService    # 服务接口
│   └── impl
│       └── HelloWorldServiceImpl  # 服务实现
└── common
    └── constant
        └── HelloWorldConstants    # 常量定义
```

## 技术栈

- **JDK**: 21
- **构建**: Maven 3.x
- **测试**: JUnit 5 + Mockito + AssertJ