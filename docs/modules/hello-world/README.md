# Hello World 模块

## 模块职责

提供 Hello World 问候服务，支持默认问候语和个性化问候语两种模式。

## 关键类说明

| 类名 | 类型 | 职责 |
|------|------|------|
| `HelloWorldService` | 接口 | 定义问候服务契约 |
| `HelloWorldServiceImpl` | 实现类 | 实现问候逻辑，含参数校验 |
| `HelloWorldController` | 控制器 | 应用入口，编排服务调用 |

## 依赖关系

- 无外部模块依赖
- 无数据库依赖
- 独立可部署模块

## API 接口列表

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `greet()` | 返回默认问候语 | 无 | `"Hello, World!"` |
| `greet(String name)` | 返回个性化问候语 | name: 用户名（非null非空白） | `"Hello, {name}!"` |

## 异常说明

| 异常 | 触发条件 |
|------|----------|
| `IllegalArgumentException` | name 为 null、空字符串或纯空白 |

## 构建与运行

```bash
# 编译
cd src/hello-world && mvn compile -DskipTests

# 运行测试
cd src/hello-world && mvn test

# 运行
cd src/hello-world && mvn exec:java -Dexec.mainClass="com.dtcoder.hello.controller.HelloWorldController"
```