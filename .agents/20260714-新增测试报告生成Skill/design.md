# 自动生成测试报告 Skill - 系统设计文档

## 1. 概述

### 1.1 背景
当前团队在完成测试执行后，测试结果散落在终端输出、CI 日志或框架原生产物中，缺乏统一格式的测试报告。本设计旨在提供一个 Skill，使 Agent 在执行测试后能够自动解析测试结果并生成结构化、可读性强的标准测试报告。

### 1.2 设计目标
- G1：一条指令自动完成测试执行、结果收集、报告生成全流程
- G2：报告内容标准化，包含摘要、明细、失败分析、覆盖率四大板块
- G3：支持主流测试框架的结果解析（Jest、Vitest、pytest、JUnit XML）
- G4：报告支持多种输出格式，默认 Markdown

### 1.3 非目标
- 不做测试用例的自动生成或修复
- 不做报告的在线托管/Web 服务化展示
- 不做多次运行结果的趋势对比分析
- 不做非测试类质量报告的聚合

---

## 2. 整体架构

### 2.1 架构图
```
┌─────────────────────────────────────────────────────────────────┐
│                      Skill 入口层                                │
│  (test-report-generation/SKILL.md)                              │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                      控制层                                      │
│  scripts/test-report-generator.ts                               │
│  - 参数解析与校验                                                │
│  - 执行模式选择                                                  │
│  - 流程编排                                                     │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                      框架检测层                                  │
│  scripts/framework-detector.ts                                  │
│  - 项目配置识别 (package.json/pyproject.toml/Cargo.toml)        │
│  - 测试框架特征文件推断                                          │
│  - 执行命令生成                                                  │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                      测试执行层                                  │
│  scripts/test-executor.ts                                       │
│  - 测试命令执行                                                  │
│  - 结果文件收集                                                  │
│  - 执行状态诊断                                                  │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                      解析器插件层                                │
│  scripts/parsers/                                               │
│  ├── jest-parser.ts          (Jest JSON)                       │
│  ├── vitest-parser.ts        (Vitest JSON)                     │
│  ├── pytest-parser.ts        (pytest JUnit XML/JSON)           │
│  └── junit-xml-parser.ts     (通用 JUnit XML)                  │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                      报告生成层                                  │
│  scripts/report-generator.ts                                    │
│  - 数据聚合与标准化                                              │
│  - 模板渲染                                                     │
│  - 文件落盘                                                     │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 模块职责

| 模块 | 职责 | 输入 | 输出 |
|------|------|------|------|
| Skill 入口层 | 定义触发意图、配置项、执行流程 | 用户意图 + 配置 | 无（文档型） |
| 控制层 | 参数解析、模式选择、流程编排 | CLI 参数 / Skill 配置 | 执行计划 |
| 框架检测层 | 识别测试框架、生成执行命令 | 项目配置文件 | 框架类型 + 测试命令 |
| 测试执行层 | 执行测试、收集结果 | 测试命令 | 结果文件路径 |
| 解析器插件层 | 解析框架特定结果格式 | 结果文件 | 标准化测试数据 |
| 报告生成层 | 生成结构化报告 | 标准化测试数据 | 报告文件 |

---

## 3. 核心流程设计

### 3.1 执行模式流程
```
开始
  │
  ▼
参数解析与校验
  │
  ├── mode=execute ────────────────┐
  │                               │
  │   框架检测                     │
  │       │                       │
  │       ▼                       │
  │   测试命令执行                 │
  │       │                       │
  │       ├── 执行失败 ──▶ 错误诊断，终止
  │       │                       │
  │       ▼                       │
  │   结果文件收集                 │
  │                               │
  ├── mode=parse ─────────────────┤
  │                               │
  │   直接读取指定结果文件          │
  │                               │
  ▼                               ▼
解析器选择
  │
  ▼
结果解析
  │
  ├── 解析失败 ──▶ 降级处理/错误报告
  │
  ▼
数据标准化
  │
  ▼
报告生成
  │
  ▼
文件落盘
  │
  ▼
返回摘要信息
  │
  ▼
结束
```

### 3.2 框架检测优先级
1. 用户显式指定的命令（test_command 参数）
2. 项目配置文件中的测试脚本：
   - package.json scripts.test（Node.js）
   - pyproject.toml 中的 pytest 配置（Python）
   - Cargo.toml 中的 test 命令（Rust）
3. 框架特征文件推断：
   - jest.config.* → Jest
   - vitest.config.* → Vitest
   - pytest.ini / conftest.py → pytest

---

## 4. 数据结构设计

### 4.1 标准化测试结果数据结构
```typescript
interface TestReport {
  // 报告头
  header: {
    projectName: string;
    generatedAt: string;        // ISO 8601
    testCommand: string;
    framework: string;
    frameworkVersion?: string;
    environment: {
      os: string;
      node?: string;
      python?: string;
    };
  };

  // 结果摘要
  summary: {
    total: number;
    passed: number;
    failed: number;
    skipped: number;
    passRate: number;           // 百分比
    duration: number;           // 毫秒
    status: 'passed' | 'failed';
  };

  // 失败用例分析
  failures: Array<{
    name: string;
    file: string;
    error: string;
    stackTrace: string[];       // 截断后的关键行
    duration?: number;
  }>;

  // 用例明细
  testSuites: Array<{
    file: string;
    tests: Array<{
      name: string;
      status: 'passed' | 'failed' | 'skipped';
      duration: number;
    }>;
  }>;

  // 覆盖率（可选）
  coverage?: {
    lines: number;              // 百分比
    statements: number;
    branches: number;
    functions: number;
    thresholdViolations?: Array<{
      file: string;
      type: string;
      value: number;
    }>;
  };

  // 附录
  appendix: {
    resultFilePaths: string[];
    generatorVersion: string;
  };
}
```

### 4.2 配置参数结构
```typescript
interface TestReportConfig {
  testCommand?: string;         // 自动检测
  resultFile?: string;          // 解析模式必需
  outputFormat: 'markdown' | 'html' | 'json';
  outputPath: string;           // 默认 reports/
  coverage: 'auto' | 'on' | 'off';
  failThreshold?: number;       // 通过率阈值
}
```

---

## 5. 解析器插件设计

### 5.1 插件接口定义
```typescript
interface TestResultParser {
  name: string;
  supportedFormats: string[];   // ['jest-json', 'vitest-json', 'junit-xml']
  
  parse(content: string): TestReport;
  
  canParse(filePath: string, content?: string): boolean;
}
```

### 5.2 解析器实现清单

#### Jest Parser
- **输入格式**: Jest JSON reporter 输出 (`--json --outputFile=result.json`)
- **关键映射**:
  - `numTotalTests` → `summary.total`
  - `numPassedTests` → `summary.passed`
  - `numFailedTests` → `summary.failed`
  - `testResults[].assertionResults[]` → 用例明细
  - `coverageMap` → 覆盖率数据

#### Vitest Parser
- **输入格式**: Vitest JSON reporter 输出 (`--reporter=json --outputFile=result.json`)
- **关键映射**: 类似 Jest，字段名略有差异

#### pytest Parser
- **输入格式**: JUnit XML (`--junit-xml=result.xml`) 或 JSON report
- **关键映射**:
  - `<testsuite tests="N">` → `summary.total`
  - `<testcase>` 元素解析 → 用例明细
  - `<failure>` 元素 → 失败分析

#### JUnit XML Parser
- **输入格式**: 标准 JUnit XML 格式
- **用途**: 通用兜底解析器
- **关键映射**: 通用 JUnit XML 结构解析

---

## 6. 报告模板设计

### 6.1 Markdown 模板结构
```markdown
# 测试报告 - {projectName}

**生成时间**: {generatedAt}  
**测试命令**: `{testCommand}`  
**框架**: {framework} ({frameworkVersion})

---

## 📊 结果摘要

| 指标 | 数值 |
|------|------|
| 用例总数 | {total} |
| ✅ 通过 | {passed} |
| ❌ 失败 | {failed} |
| ⏭️ 跳过 | {skipped} |
| 通过率 | {passRate}% |
| 执行耗时 | {duration}ms |

**整体结论**: {status}

---

## ❌ 失败用例分析

<!-- 仅当有失败时显示 -->

### {failure.name}

- **文件**: `{failure.file}`
- **错误信息**: 
  ```
  {failure.error}
  ```
- **堆栈关键行**:
  ```
  {failure.stackTrace}
  ```

---

## 📋 用例明细

<!-- 按测试文件分组，超过200条截断 -->

### {testSuite.file}

| 用例名 | 状态 | 耗时 |
|--------|------|------|
| {test.name} | {status} | {duration}ms |

---

## 📈 覆盖率

| 类型 | 覆盖率 |
|------|--------|
| 语句 | {statements}% |
| 分支 | {branches}% |
| 函数 | {functions}% |
| 行 | {lines}% |

<!-- 若覆盖率低于阈值，列出违规文件 -->

---

## 📎 附录

- **原始结果文件**: `{resultFilePaths}`
- **生成工具版本**: v{generatorVersion}
```

### 6.2 截断与折叠策略
- 用例明细超过 200 条时，显示前 100 条 + "...省略 N 条" + 后 100 条
- 失败用例堆栈截断至 20 行
- 错误信息截断至 500 字符

---

## 7. 错误处理与降级策略

### 7.1 错误分类与处理

| 错误类型 | 场景 | 处理策略 |
|----------|------|----------|
| 执行失败 | 测试命令无法运行 | 返回诊断信息，不生成报告 |
| 结果文件缺失 | 指定路径不存在 | 返回明确错误，提示正确路径 |
| 格式不支持 | 解析器无法识别 | 返回支持的格式列表 |
| 字段缺失 | 结果数据不完整 | 标注"未获取"，其余正常生成 |
| 覆盖率不可用 | 无覆盖率数据 | 标注"未获取"或跳过章节 |

### 7.2 降级输出规范
- 缺失字段统一标注：`未获取`
- 覆盖率章节不存在时显示：`> 覆盖率数据未获取`
- 时间戳缺失时使用当前时间

---

## 8. 安全性设计

### 8.1 敏感信息过滤
- 错误堆栈中过滤环境变量（`process.env.*`）
- 过滤路径中的敏感目录（`/home/{user}/`, `C:\Users\{user}\`）
- 不输出密钥、Token 类内容

### 8.2 文件路径处理
- 使用相对路径或标准化路径
- 不暴露系统绝对路径结构

---

## 9. 性能设计

### 9.1 性能目标
- 结果解析与报告生成（不含测试执行）：< 5s（1000 用例）

### 9.2 性能优化策略
- 流式解析大文件
- 并行解析多个结果文件
- 模板渲染缓存
- 截断策略避免大报告

---

## 10. Skill 定义

### 10.1 SKILL.md 元数据
```yaml
---
name: test-report-generation
description: 自动解析测试结果并生成结构化测试报告
tags: [testing, report, automation, quality]
activation_mode: explicit
---
```

### 10.2 触发意图
- "生成测试报告"
- "跑一下测试并出报告"
- "把这个 junit.xml 转成测试报告"
- "测试报告"

### 10.3 Skill 执行流程
1. **参数解析**: 从用户意图提取配置（命令、路径、格式）
2. **模式判断**: 执行模式 vs 解析模式
3. **框架检测**: 自动识别测试框架（执行模式）
4. **测试执行**: 运行测试命令并收集结果（执行模式）
5. **结果解析**: 选择解析器并解析结果文件
6. **报告生成**: 渲染模板并落盘
7. **返回摘要**: 报告路径 + 核心指标

---

## 11. 里程碑规划

### M1 - P0（首期）
- Jest/Vitest JSON 解析
- JUnit XML 解析（通用兜底）
- Markdown 报告生成
- 执行/解析双模式
- 基础错误处理

### M2 - P1
- pytest 支持
- 覆盖率章节
- fail_threshold 配置
- 敏感信息过滤

### M3 - P1
- HTML 输出格式
- JSON 伴随产物

### M4 - P2（后续迭代）
- 历史趋势对比
- 更多框架支持（Go test / cargo test）
- 自定义模板

---

## 12. 文件结构

```
skills/
└── test-report-generation/
    ├── SKILL.md
    └── scripts/
        ├── test-report-generator.ts    # 主入口
        ├── framework-detector.ts       # 框架检测
        ├── test-executor.ts            # 测试执行
        ├── report-generator.ts         # 报告生成
        └── parsers/
            ├── jest-parser.ts
            ├── vitest-parser.ts
            ├── pytest-parser.ts
            └── junit-xml-parser.ts
```

---

## 13. 验收标准

| 编号 | 验收项 | 验证方法 |
|------|--------|----------|
| AC1 | Jest/Vitest 项目生成 Markdown 报告，结构符合 4.2 | 执行 Skill，检查报告文件 |
| AC2 | 失败用例包含用例名、文件路径、错误信息 | 人工制造失败用例，验证报告内容 |
| AC3 | JUnit XML 解析模式不触发测试执行 | 提供现成 XML，验证无测试命令执行 |
| AC4 | 结果文件损坏返回明确错误 | 提供损坏文件，验证错误信息 |
| AC5 | 覆盖率缺失时标注"未获取" | 移除覆盖率数据，验证报告显示 |

---

## 14. 风险与开放问题

### 14.1 已识别风险
| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| R1: 各框架 reporter 输出差异大 | 解析复杂度 | 插件式设计，隔离框架差异 |
| R2: 测试执行耗时不可控 | Agent 超时 | 后台执行 + 轮询（依赖运行时能力） |
| R3: 覆盖率数据格式不统一 | 解析困难 | 框架特定解析器处理 |

### 14.2 开放问题
| 编号 | 问题 | 建议 |
|------|------|------|
| Q1 | 首期目标项目栈 | 假设 TypeScript/Node 为主 |
| Q2 | 报告语言 | 支持中文/英文双语模板 |
| Q3 | 报告推送渠道 | 列为非目标，后续迭代 |

---

## 15. 附录

### 15.1 参考文档
- Jest JSON Output: https://jestjs.io/docs/cli#--outputfile
- Vitest JSON Reporter: https://vitest.dev/config/#reporters
- pytest JUnit XML: https://docs.pytest.org/en/stable/how-to/output_formats.html
- JUnit XML Schema: https://junit.org/junit5/docs/current/user-guide/#running-tests-build

### 15.2 术语表
| 术语 | 定义 |
|------|------|
| Skill | Agent 可调用的能力包，包含指令、流程、脚本 |
| 解析模式 | 仅解析已有结果文件，不触发测试执行 |
| 执行模式 | 自动执行测试并收集结果 |

---

**文档版本**: v1.0  
**创建日期**: 2026-07-14  
**作者**: Agent Design System