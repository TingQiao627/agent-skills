---
name: test-report-generator
description: 自动解析测试结果并生成结构化标准测试报告，支持 Jest/Vitest/pytest/JUnit XML，提供执行和解析双模式
tags: [testing, reports, jest, vitest, pytest, junit]
---

# 测试报告生成 Skill

## 概述

本 Skill 在测试执行后自动解析测试结果，生成结构化、可读性强的标准测试报告。

## 何时使用

- 执行测试后需要生成标准化报告
- 需要将 JUnit XML/JSON 结果文件转换为可读报告
- CI 流程中需要将测试结果归档和传播

## 工作流程

### Step 1: 模式检测

Skill 首先判断工作模式：
- **执行模式**：自动检测测试框架并运行测试，然后解析结果
- **解析模式**：直接解析用户指定的已有结果文件

### Step 2: 框架识别

自动识别项目使用的测试框架：
1. 用户显式指定的命令（优先级最高）
2. package.json scripts（test）、pyproject.toml、Cargo.toml
3. 框架配置文件（jest.config.*、vitest.config.*、pytest.ini）

### Step 3: 结果解析

根据框架类型调用对应解析器：
- **Jest/Vitest**: 解析 JSON reporter 输出
- **pytest**: 解析 JUnit XML 或 JSON report
- **JUnit XML**: 通用解析器（跨语言兜底）

### Step 4: 报告生成

生成包含以下章节的标准化报告：
1. 报告头：项目名、生成时间、执行命令、框架/版本
2. 结果摘要：用例总数、通过/失败/跳过、通过率、耗时
3. 失败用例分析：错误信息、堆栈摘要、文件定位
4. 用例明细：按文件分组，超过200条截断
5. 覆盖率：语句/分支/函数/行覆盖率
6. 附录：原始文件路径、工具版本

## 配置项

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| test_command | 自动检测 | 测试执行命令 |
| result_file | 自动检测 | 解析模式下的结果文件 |
| output_format | markdown | markdown / html / json |
| output_path | reports/ | 报告输出目录 |
| coverage | auto | auto / on / off |
| fail_threshold | 无 | 通过率阈值 |

## 验收标准

- ✅ 在 Jest/Vitest 项目中执行生成命令，产出符合结构的 Markdown 报告
- ✅ 存在失败用例时，报告包含用例名、文件路径、错误信息
- ✅ 提供 JUnit XML 文件可仅解析不执行测试
- ✅ 结果文件损坏时返回明确错误说明
- ✅ 覆盖率数据不存在时标注"未获取"

## 相关资源

- [Jest JSON Reporter](https://jestjs.io/docs/configuration#reporters-arraymodulename--options)
- [Vitest Reporters](https://vitest.dev/config/#reporters)
- [JUnit XML Schema](https://maven.apache.org/surefire/maven-surefire-plugin/xsd/surefire-test-report.xsd)