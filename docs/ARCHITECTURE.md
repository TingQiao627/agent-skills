# 项目架构文档

## 模块列表

| 模块 | 路径 | 说明 |
|------|------|------|
| hello | `src/main/java/com/example/hello/` | Hello World 问候服务 |

## 分层架构

```
com.example.hello
├── model/              # 数据模型层（VO）
├── HelloWorldService        # 业务接口层
├── HelloWorldServiceImpl    # 业务实现层
├── HelloWorldController     # 控制器层
└── HelloWorldApplication    # 应用入口
```

## 技术栈

- Java（无框架依赖，纯 POJO）
- 单元测试：JUnit 5 + AssertJ

## 约束

- 包名小写，点分隔
- 类名大驼峰，方法名小驼峰
- 接口方法不加 public 修饰符
- Service 实现类使用 Impl 后缀