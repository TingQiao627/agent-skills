# 项目架构文档

## 模块列表

| 模块 | 语言 | 说明 | 文档 |
|------|------|------|------|
| account-login | Python | T3 账号登录系统 (FastAPI) | `src/account-login/` |
| file-service | Python | 文件服务 | `src/file-service/` |
| hello-world | Java | Hello World 示例（数科编码规范演示） | `docs/modules/hello-world/README.md` |

## 技术栈

- **Python 服务**: FastAPI
- **Java 示例**: JDK 11+（纯标准库，无构建系统依赖）

## 分层约束

本仓库为多语言混合仓库，Java 模块遵循：

```
src/main/java/com/example/   # 业务代码
src/test/java/com/example/   # 单元测试
docs/modules/{module}/       # 模块文档
```

## 编码规范

- Java 代码遵循 `dtazziboot-java-coding-standards` 技能规约
- Python 代码遵循项目既有约定