---
name: generate-test-report
description: Use when user requests test report generation, parsing test results, or converting JUnit XML/JSON test output to structured reports. Triggers on phrases like "生成测试报告", "跑测试并出报告", "parse junit.xml", or when test execution completes and results need summarization.
---

# 自动生成测试报告

## Overview

自动化测试结果收集、解析与报告生成。支持执行模式（自动运行测试）和解析模式（仅解析已有结果文件），生成包含摘要、明细、失败分析、覆盖率的标准测试报告。

## When to Use

**触发条件：**
- 用户请求 "生成测试报告"、"跑测试并出报告"、"generate test report"
- 用户指定已有结果文件需要解析："把这个 junit.xml 转成报告"、"分析测试结果文件 <path>"
- CI/CD 流程中需要从现有测试输出生成报告
- 测试执行完成后需要结构化结果汇总

**不适用场景：**
- 需要生成测试用例（仅处理结果报告）
- 需要在线托管/Web 展示服务
- 需要多次运行趋势对比

## Process

### Step 1: 意图识别与模式选择

根据用户指令判断工作模式：

| 触发词例 | 模式 |
|----------|------|
| "生成测试报告"、"跑测试并出报告" | 执行模式 |
| "把这个 junit.xml 转成报告"、"分析测试结果文件 <path>" | 解析模式 |

**配置参数（均有默认值）：**
- `test_command`: 自动检测 | 显式指定测试命令
- `result_file`: 自动检测 | 解析模式结果文件路径
- `output_format`: markdown（默认）| html | json
- `output_path`: reports/（默认）
- `coverage`: auto（默认）| on | off
- `fail_threshold`: 无（默认）| 通过率阈值百分比

### Step 2: 框架识别（执行模式）

识别优先级：
1. 用户显式指定
2. 项目配置文件（package.json scripts、pyproject.toml、Cargo.toml）
3. 框架特征文件

**P0 支持框架：**
- **Jest**: 检测 `jest.config.js/ts`、`package.json` scripts.test
- **Vitest**: 检测 `vitest.config.ts`、`vite.config.ts`
- **pytest**: 检测 `pytest.ini`、`pyproject.toml` [tool.pytest]
- **JUnit XML**: 通用兜底，检测 `*.xml` 文件

**配置文件解析逻辑：**

| 配置文件 | 解析方式 | 测试命令提取 |
|---------|---------|-------------|
| `package.json` | JSON 解析 | `scripts.test` 字段值 |
| `pyproject.toml` | TOML 解析 | `[tool.pytest]` 配置或 `pytest` 命令 |
| `Cargo.toml` | TOML 解析 | `[scripts.test]` 或 `cargo test` |

**识别失败降级策略：**
1. 尝试通用命令：`npm test`、`pytest`、`cargo test`
2. 返回明确错误：提供支持的框架列表和手动指定方式
3. 不生成空报告，返回诊断信息

### Step 3: 测试执行（执行模式）

**Jest 命令（含覆盖率）：**
```bash
jest --json --outputFile=jest-output.json --testLocationInResults --coverage --coverageReporters=json
```
覆盖率数据位于 `jest-output.json` 的 `coverageMap` 字段

**Vitest 命令（含覆盖率）：**
```bash
vitest run --reporter=json --outputFile=vitest-output.json --coverage
```
需配置 `vitest.config.ts` 中 `coverage.provider: 'v8'` 或 `'istanbul'`

**pytest 命令（含覆盖率）：**
```bash
pytest --junit-xml=pytest-results.xml --cov --cov-report=json
```
需要安装 `pytest-cov` 插件

**覆盖率配置行为：**
| coverage 值 | 行为 |
|------------|------|
| `auto` | 自动检测覆盖率工具，存在则启用 |
| `on` | 强制启用覆盖率，缺失时报错 |
| `off` | 跳过覆盖率收集 |

**错误处理：**
- 命令不存在 → 返回明确诊断信息，不生成空报告
- 执行失败 → 收集 stderr，生成包含错误信息的报告

### Step 4: 结果解析

**解析器选择逻辑：**
1. 根据文件扩展名初步判断（.json / .xml）
2. 内容嗅验确认格式
3. 调用对应解析器

**解析器接口定义：**

**1. TestResultParser 接口（插件式架构）：**
```typescript
interface TestResultParser {
  name: string;                              // 解析器名称（如 'jest', 'vitest'）
  supportedFormats: string[];                // 支持的文件格式（如 ['.json', '.xml']）
  priority: number;                          // 优先级（数字越小优先级越高）
  canParse(filePath: string, content: string): boolean;  // 判断是否能解析
  parse(content: string): ParsedTestResult;  // 解析逻辑
}
```

**解析器注册与发现机制：**
- 解析器按优先级排序，优先使用高优先级解析器
- 检测顺序：文件扩展名 → canParse 嗅验 → 调用 parse
- 解析失败时降级至下一个解析器（如果存在）

**2. ParsedTestResult 接口：**
```typescript
interface ParsedTestResult {
  summary: {
    total: number;
    passed: number;
    failed: number;
    skipped: number;
    duration: number;    // 毫秒
    passRate: number;    // 百分比
  };
  testCases: Array<{
    name: string;
    file: string;
    status: 'passed' | 'failed' | 'skipped';
    duration: number;
  }>;
  failures: Array<{
    testCaseName: string;
    file: string;
    errorMessage: string;
    stackTrace: string;  // 截断至20行
  }>;
  coverage?: {
    statements: number;
    branches: number;
    functions: number;
    lines: number;
    lowCoverageFiles?: string[];
  };
  metadata: {
    framework: string;
    version?: string;
    timestamp: string;
    command?: string;
  };
}
```

**3. 安全过滤机制（敏感信息保护）：**

在解析结果后执行以下安全过滤：

| 过滤类型 | 正则规则 | 替换为 |
|---------|---------|--------|
| 环境变量 | `process\.env\.[A-Z_]+` | `[ENV_VAR]` |
| 密钥标识 | `(password|secret|token|key|auth)[\"']?\\s*[:=]\\s*[\"']?[^,}\"'\\s]+` | `[REDACTED]` |
| 敏感路径 | `/home/[^/]+|/Users/[^/]+|C:\\Users\\[^\\]+` | `[HOME]` |
| API Keys | `(sk-[a-zA-Z0-9]{20,}|ghp_[a-zA-Z0-9]{36})` | `[API_KEY]` |

**堆栈信息清洗：**
- 过滤包含 `node_modules` 的路径
- 过滤包含 `.env`、`config` 的文件路径
- 截断堆栈至关键 20 行（保留最相关的错误上下文）

**命令注入防护：**
- 禁止 `test_command` 参数包含 shell 元字符：`|`, `;`, `&`, `$`, `` ` ``, `(`, `)`, `{`, `}`, `<`, `>`
- 使用参数化执行而非字符串拼接

### Step 5: 报告生成

**报告结构（固定顺序）：**

#### 1. 报告头
- 项目名
- 生成时间：`YYYY-MM-DD HH:mm:ss`
- 执行命令
- 框架/版本
- 执行环境摘要

#### 2. 结果摘要
```
| 指标 | 值 |
|------|-----|
| 用例总数 | N |
| 通过 | N |
| 失败 | N |
| 跳过 | N |
| 通过率 | XX.X% |
| 总耗时 | XXs |
| 结论 | ✅ PASS / ❌ FAIL |
```

#### 3. 失败用例分析（有失败时必选）
每条失败用例包含：
- 用例名
- 所属文件
- 错误信息
- 堆栈关键行（截断至20行）

#### 4. 用例明细
- 按测试文件分组
- 各自耗时
- 超过200条截断并注明

#### 5. 覆盖率（若可获取）
```
| 类型 | 覆盖率 |
|------|--------|
| 语句 | XX.X% |
| 分支 | XX.X% |
| 函数 | XX.X% |
| 行 | XX.X% |
```
低于阈值的文件清单

#### 6. 附录
- 原始结果文件路径
- 生成工具版本

### Step 6: 输出落盘

**默认路径规则：**
- Markdown: `reports/test-report-<YYYYMMDD-HHmmss>.md`
- HTML: `reports/test-report-<YYYYMMDD-HHmmss>.html`
- JSON: `reports/test-report-<YYYYMMDD-HHmmss>.json`

**返回给用户：**
- 报告路径
- 结果摘要（通过率、失败数）
- 失败时的关键失败原因（1-3条）

## Quick Reference

### 配置项清单

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| test_command | 自动检测 | 测试执行命令 |
| result_file | 自动检测 | 解析模式结果文件路径 |
| output_format | markdown | markdown / html / json |
| output_path | reports/ | 报告输出目录 |
| coverage | auto | auto / on / off |
| fail_threshold | 无 | 通过率阈值 |

### 支持框架

| 框架 | 语言 | 结果格式 | P0 |
|------|------|----------|-----|
| Jest | JS/TS | JSON | ✅ |
| Vitest | JS/TS | JSON | ✅ |
| pytest | Python | JUnit XML | P1 |
| JUnit XML | 通用 | XML | ✅ |

## Common Mistakes

### ❌ 测试命令不存在时生成空报告
**正确做法：** 返回明确诊断信息
```json
{
  "error": "TEST_COMMAND_NOT_FOUND",
  "message": "未找到测试命令。请确认项目配置了测试脚本",
  "suggestion": "尝试: npm test 或 pytest"
}
```

### ❌ 结果文件格式异常时崩溃
**正确做法：** 降级输出，缺失项标注"未获取"

### ❌ 报告中泄露敏感信息
**正确做法：** 过滤环境变量、密钥路径、process.env 等

### ❌ 覆盖率不存在时报告失败
**正确做法：** 标注"未获取"，其余章节正常输出

## Verification

**验收标准：**

| 标准 | 验证方法 |
|------|----------|
| AC1: Jest/Vitest 项目生成 Markdown 报告 | 在含 Jest/Vitest 的 TS 项目执行，检查报告结构符合 FR2 |
| AC2: 失败用例含完整信息 | 检查失败用例包含用例名、文件路径、错误信息 |
| AC3: 解析模式不触发测试执行 | 指定已有 junit.xml，确认无测试命令执行 |
| AC4: 结果文件损坏时返回明确错误 | 提供损坏文件，确认返回诊断信息而非空报告 |
| AC5: 覆盖率不存在时正常输出 | 无覆盖率数据项目，确认报告含"未获取"标注 |

**性能要求：**
- 结果解析与报告生成：5秒内（1000用例规模）
- 报告文件大小：单文件 < 10MB

## Real-World Impact

- **效率提升：** 从人工收集整理耗时30分钟 → 自动生成30秒
- **格式统一：** 跨项目/跨团队沟通成本降低
- **失败定位：** 失败用例直接提供上下文，无需回溯日志
- **质量沉淀：** 覆盖率、通过率等指标可追踪