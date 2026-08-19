# 架构文档

## 项目概述

数科技能仓库（DTCoder Skills Repository）— Claude Code 技能与智能体集合。

## 模块列表

| 模块 | 技术栈 | 说明 |
|------|--------|------|
| account-login | Python | 账户登录服务 |
| file-service | Python | 文件服务 |
| hello-world | Java SE | Hello World 示例模块 |

## 分层架构

```
src/
├── account-login/     # Python 账户登录模块
├── file-service/      # Python 文件服务模块
└── hello-world/       # Java Hello World 示例
    ├── src/main/java/  # 源代码
    └── src/test/java/  # 单元测试
```

## 技术约束

- Java 模块：JDK 21 LTS，遵循 `dtazziboot-java-coding-standards` 规范
- Python 模块：遵循现有项目约定