# hello-world 模块

## 模块职责

Hello World 示例模块，演示符合数科 Java 编码规范的标准化模块结构。

## 关键类说明

| 类名 | 类型 | 说明 |
|------|------|------|
| `HelloWorldApplication` | 应用入口 | Spring Boot 启动类 |
| `HelloWorldController` | REST 控制器 | 提供 `/api/hello` HTTP 接口 |
| `HelloWorldService` | 服务接口 | 欢迎消息生成接口 |
| `HelloWorldServiceImpl` | 服务实现 | 欢迎消息生成默认实现 |
| `HelloWorldServiceImplTest` | 单元测试 | 覆盖 5 个测试场景 |

## 依赖关系

- 无内部模块依赖
- 外部依赖：Spring Boot 3.2.5 (spring-boot-starter-web)

## API 接口列表

| 方法 | 路径 | 参数 | 说明 |
|------|------|------|------|
| GET | `/api/hello` | `name` (可选) | 返回欢迎消息 |

### 请求示例

```bash
# 默认消息
curl http://localhost:8080/api/hello
# 输出: Hello, World!

# 带名称
curl "http://localhost:8080/api/hello?name=DTCoder"
# 输出: Hello, DTCoder!
```

## 启动方式

```bash
cd src/hello-world
mvn spring-boot:run
```

## 测试

```bash
cd src/hello-world
mvn test
```