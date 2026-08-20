# 项目架构文档

## 项目概述

Agent Skills 仓库，提供 Claude Code 的编码代理技能与指令集。同时包含示例模块用于验证技能规范。

## 模块列表

| 模块 | 技术栈 | 说明 |
|------|--------|------|
| `src/account-login` | Python | 账户登录服务 |
| `src/file-service` | Python | 文件服务 |
| `src/hello-world` | Java 21 / Maven | Hello World 示例模块（遵循 dtazziboot 编码规范） |

## 分层架构

- **skills/**: 技能定义（SKILL.md + references）
- **agents/**: 角色定义（Persona）
- **commands/**: 用户入口（Slash Commands）
- **src/**: 示例业务模块
- **docs/**: 项目文档

## 技术约束

- Java 模块：JDK 21，Maven 构建，遵循 dtazziboot-java-coding-standards 规范
- Python 模块：Python 3.x