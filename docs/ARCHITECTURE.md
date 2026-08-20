# 架构文档

## 项目概述

数科业务技能仓库，包含 AI 编码智能体所需的技能、代理和命令配置。

## 模块列表

| 模块 | 路径 | 技术栈 | 说明 |
|------|------|--------|------|
| account-login | src/account-login/ | Python | 账户登录服务 |
| file-service | src/file-service/ | Python | 文件服务 |
| hello-world | src/main/java/com/example/ | Java | Hello World 示例模块 |

## 分层架构

```
src/
├── main/java/com/example/     # Java 示例模块
├── account-login/             # Python 账户登录服务
└── file-service/              # Python 文件服务
```

## 约束

- Python 模块位于 `src/` 下各业务目录
- Java 模块遵循 Maven 标准目录结构
- 无统一构建系统，各模块独立管理