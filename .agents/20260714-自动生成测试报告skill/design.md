# 自动生成测试报告 Skill - 系统分析设计文档

**文档版本**: 1.0  
**生成时间**: 2026-07-14  
**状态**: 设计阶段

---

## 1. 概述

### 1.1 背景

当前团队在完成测试执行后，测试结果散落在终端输出、CI 日志或框架原生产物中，存在结果收集耗时、格式不统一、失败用例回溯困难、质量指标难以沉淀等痛点。

### 1.2 目标

设计并实现一个 Skill，使 Agent 在执行测试后能够自动解析测试结果并生成结构化、可读性强的标准测试报告。

### 1.3 核心价值

- **G1**: 一条指令自动完成测试执行 → 结果收集 → 报告生成
- **G2**: 报告内容标准化（摘要、明细、失败分析、覆盖率）
- **G3**: 支持主流测试框架（Jest、Vitest、pytest、JUnit XML）
- **G4**: 多输出格式（默认 Markdown，支持 HTML、JSON）

---

## 2. 系统架构

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                         Skill 入口层                             │
│                    (意图识别 & 参数解析)                          │
└────────────────────────┬────────────────────────────────────────┘
                         │
         ┌───────────────┼───────────────┐
         │               │               │
         ▼               ▼               ▼
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│  执行模式    │  │  解析模式    │  │  配置模式    │
│ (Execute)   │  │ (Parse)     │  │ (Config)    │
└──────┬──────┘  └──────┬──────┘  └─────────────┘
       │                │
       ▼                ▼
┌─────────────────────────────────────┐
│        测试框架识别器                 │
│    (Framework Detector)              │
└────────────────┬────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────┐
│        结果解析器总线                 │
│    (Parser Bus - Plugin Pattern)    │
├─────────────────────────────────────┤
│  Jest Parser  │  Vitest Parser      │
│  Pytest Parser│  JUnit XML Parser   │
└────────────────┬────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────┐
│        报告生成器                     │
│    (Report Generator)               │
├─────────────────────────────────────┤
│  Markdown  │  HTML  │  JSON         │
└────────────────┬────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────┐
│        输出落盘                       │
│    (Output Writer)                  │
└─────────────────────────────────────┘
```

### 2.2 核心模块划分

| 模块 | 职责 | 依赖 |
|------|------|------|
| 入口层 | 意图识别、参数解析、模式选择 | 配置管理 |
| 框架识别器 | 自动检测项目使用的测试框架 | 无 |
| 执行引擎 | 触发测试命令并收集结果 | 框架识别器 |
| 解析器总线 | 插件式解析器管理、结果归一化 | 各框架解析器 |
| 报告生成器 | 结构化报告渲染 | 模板引擎 |
| 输出落盘 | 文件写入、路径管理 | 文件系统 |

---

## 3. 核心组件设计

### 3.1 入口层 (Entry Point)

**职责**: 识别用户意图，解析配置参数，选择工作模式。

**输入**: 用户自然语言指令

**输出**: 执行上下文对象

```typescript
interface ExecutionContext {
  mode: 'execute' | 'parse';           // 工作模式
  testCommand?: string;                 // 显式指定的测试命令
  resultFile?: string;                  // 解析模式下的结果文件路径
  outputFormat: 'markdown' | 'html' | 'json';
  outputPath: string;                   // 报告输出目录
  coverage: 'auto' | 'on' | 'off';
  failThreshold?: number;               // 通过率阈值
}
```

**意图识别规则**:

| 触发词例 | 识别模式 |
|----------|----------|
| "生成测试报告"、"跑测试并出报告" | 执行模式 |
| "把这个 junit.xml 转成报告" | 解析模式 |
| "分析测试结果文件 <path>" | 解析模式 |

### 3.2 框架识别器 (Framework Detector)

**职责**: 自动识别项目使用的测试框架。

**识别优先级**:
1. 用户显式指定
2. 项目配置文件（package.json scripts、pyproject.toml、Cargo.toml）
3. 框架特征文件（jest.config.*、vitest.config.*、pytest.ini）

**支持框架** (P0):

| 框架 | 语言 | 结果格式 | 识别特征 |
|------|------|----------|----------|
| Jest | JS/TS | JSON (jest-output.json) | jest.config.js/ts |
| Vitest | JS/TS | JSON (vitest-output.json) | vitest.config.ts |
| pytest | Python | JUnit XML / JSON | pytest.ini, pyproject.toml |
| JUnit XML | 通用 | XML | *.xml (junit 格式) |

### 3.3 解析器总线 (Parser Bus)

**设计模式**: 插件式架构 (Plugin Pattern)

**接口定义**:

```typescript
interface TestResultParser {
  name: string;                                    // 解析器名称
  supportedFormats: string[];                      // 支持的格式列表
  canParse(filePath: string, content: string): boolean;  // 能否解析
  parse(content: string): ParsedTestResult;       // 解析逻辑
}

interface ParsedTestResult {
  // 统一结果结构
  summary: TestSummary;
  testCases: TestCase[];
  failures: FailureDetail[];
  coverage?: CoverageData;
  metadata: ResultMetadata;
}

interface TestSummary {
  total: number;
  passed: number;
  failed: number;
  skipped: number;
  duration: number;       // 毫秒
  passRate: number;       // 百分比
}

interface TestCase {
  name: string;
  file: string;
  status: 'passed' | 'failed' | 'skipped';
  duration: number;
}

interface FailureDetail {
  testCaseName: string;
  file: string;
  errorMessage: string;
  stackTrace: string;     // 截断至可读长度
}

interface CoverageData {
  statements: number;     // 百分比
  branches: number;
  functions: number;
  lines: number;
  lowCoverageFiles?: string[];  // 低于阈值的文件
}

interface ResultMetadata {
  framework: string;
  version?: string;
  timestamp: string;
  command?: string;
}
```

### 3.4 报告生成器 (Report Generator)

**职责**: 将解析后的数据渲染为结构化报告。

**报告结构** (符合 FR2):

```
1. 报告头
   - 项目名
   - 生成时间
   - 执行命令
   - 框架/版本
   - 执行环境摘要

2. 结果摘要
   - 用例总数
   - 通过/失败/跳过数
   - 通过率
   - 总耗时
   - 整体结论 (✅/❌)

3. 失败用例分析 (有失败时必选)
   - 用例名
   - 所属文件
   - 错误信息
   - 堆栈关键行

4. 用例明细
   - 按测试文件分组
   - 各自耗时
   - 超过200条截断并注明

5. 覆盖率 (若可获取)
   - 语句/分支/函数/行覆盖率总表
   - 低于阈值的文件清单

6. 附录
   - 原始结果文件路径
   - 生成工具版本
```

### 3.5 输出落盘 (Output Writer)

**职责**: 管理报告文件的写入和路径。

**默认路径规则**:
- Markdown: `reports/test-report-<YYYYMMDD-HHmmss>.md`
- HTML: `reports/test-report-<YYYYMMDD-HHmmss>.html`
- JSON: `reports/test-report-<YYYYMMDD-HHmmss>.json`

**返回给用户**:
- 报告路径
- 结果摘要（通过率、失败数）
- 失败时的关键失败原因（1-3条）

---

## 4. 数据流设计

### 4.1 执行模式流程

```
用户指令
    │
    ▼
[意图识别] ────┬─── 执行模式
               │
               ▼
        [框架识别]
               │
               ▼
        [命令构建] ──── jest --json --outputFile=jest-output.json
               │
               ▼
        [测试执行] ──── 收集 stdout/stderr
               │
               ▼
        [结果解析] ──── Parser.parse()
               │
               ▼
        [报告生成] ──── Markdown/HTML/JSON
               │
               ▼
        [落盘输出] ──── reports/test-report-*.md
               │
               ▼
        [返回摘要]
```

### 4.2 解析模式流程

```
用户指令 + 结果文件路径
    │
    ▼
[意图识别] ──── 解析模式
    │
    ▼
[格式检测] ──── 根据 file extension + content sniff
    │
    ▼
[解析器选择] ──── Parser.canParse()
    │
    ▼
[结果解析] ──── Parser.parse()
    │
    ▼
[报告生成]
    │
    ▼
[落盘输出]
```

---

## 5. 技术设计细节

### 5.1 Jest 解析器实现要点

**命令**: `jest --json --outputFile=jest-output.json --testLocationInResults`

**JSON 结构关键字段**:
```json
{
  "success": boolean,
  "numTotalTests": number,
  "numPassedTests": number,
  "numFailedTests": number,
  "numPendingTests": number,
  "testResults": [{
    "name": "文件路径",
    "status": "passed/failed",
    "assertionResults": [{
      "fullName": "用例名",
      "status": "passed/failed",
      "duration": number,
      "failureMessages": []
    }]
  }],
  "coverageMap": {}  // 可选
}
```

### 5.2 Vitest 解析器实现要点

**命令**: `vitest run --reporter=json --outputFile=vitest-output.json`

**JSON 结构**: 与 Jest 类似但字段名可能不同，需映射。

### 5.3 pytest 解析器实现要点

**命令**: `pytest --junit-xml=pytest-results.xml --json-report --json-report-file=pytest-results.json`

**JUnit XML 结构**:
```xml
<testsuite name="pytest" tests="10" failures="2" skipped="1">
  <testcase name="test_example" classname="test_module" time="0.5">
    <failure message="AssertionError">堆栈内容</failure>
  </testcase>
</testsuite>
```

### 5.4 通用 JUnit XML 解析器

作为跨语言兜底格式，支持任何输出 JUnit XML 的框架。

**解析逻辑**:
1. 解析 `<testsuite>` 获取汇总数据
2. 遍历 `<testcase>` 提取用例明细
3. 提取 `<failure>` 内容作为错误信息

---

## 6. 错误处理与健壮性

### 6.1 错误分类

| 错误类型 | 处理策略 |
|----------|----------|
| 测试命令不存在 | 返回明确诊断信息，不生成空报告 |
| 结果文件格式异常 | 降级输出，缺失项标注"未获取" |
| 解析器不匹配 | 尝试下一个解析器，全部失败则报错 |
| 字段缺失 | 使用默认值，标注"未获取" |
| 敏感信息泄露 | 过滤堆栈中的环境变量、密钥路径 |

### 6.2 诊断信息示例

```json
{
  "error": "TEST_COMMAND_NOT_FOUND",
  "message": "未找到测试命令。请确认项目配置了测试脚本，或显式指定 --test-command",
  "suggestion": "尝试: npm test 或 pytest"
}
```

---

## 7. 性能要求

| 指标 | 要求 |
|------|------|
| 结果解析与报告生成 | 5秒内 (1000用例) |
| 报告文件大小 | 单文件 < 10MB |

**优化策略**:
- 流式解析大文件
- 用例明细截断（超过200条）
- 堆栈截断（最多保留关键20行）

---

## 8. 安全设计

| 风险 | 缓解措施 |
|------|----------|
| 环境变量泄露 | 过滤报告中的 `process.env`、`SECRET_*` 等 |
| 密钥路径泄露 | 正则匹配并替换敏感路径 |
| 命令注入 | 参数校验，禁止 shell 元字符 |

---

## 9. Skill 文件结构

```
skills/generate-test-report/
├── SKILL.md                    # 技能主文档
├── scripts/
│   ├── parsers/
│   │   ├── jest-parser.ts
│   │   ├── vitest-parser.ts
│   │   ├── pytest-parser.ts
│   │   └── junit-xml-parser.ts
│   ├── generators/
│   │   ├── markdown-generator.ts
│   │   ├── html-generator.ts
│   │   └── json-generator.ts
│   ├── detector.ts             # 框架识别器
│   └── index.ts                # 入口
└── templates/
    ├── report.md.tpl
    └── report.html.tpl
```

---

## 10. 验收标准映射

| 验收标准 | 设计支持 |
|----------|----------|
| AC1: Jest/Vitest 项目生成 Markdown 报告 | FR2 报告结构 + Jest/Vitest 解析器 |
| AC2: 失败用例含用例名、文件、错误信息 | FailureDetail 数据结构 |
| AC3: 解析模式不触发测试执行 | 模式选择逻辑 |
| AC4: 结果文件损坏时返回明确错误 | 错误处理机制 |
| AC5: 覆盖率不存在时标注"未获取" | CoverageData 可选字段 |

---

## 11. 里程碑规划

| 阶段 | 范围 | 优先级 |
|------|------|--------|
| M1 | Jest/Vitest JSON + JUnit XML 解析、Markdown 报告、执行/解析双模式 | P0 |
| M2 | pytest 支持、覆盖率章节、fail_threshold | P1 |
| M3 | HTML 输出、JSON 伴随产物 | P1 |
| M4 | 历史趋势对比、更多框架 | P2 |

---

## 12. 风险与开放问题

### 12.1 已识别风险

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| R1: 各框架 reporter 输出差异大 | 解析复杂度高 | NFR5 插件式设计，隔离变化 |
| R2: 测试执行耗时不可控 | 用户体验差 | 后台执行 + 轮询机制 |

### 12.2 开放问题

| 问题 | 建议 |
|------|------|
| Q1: 首期目标项目栈 | 以 TypeScript/Node 为主 (P0) |
| Q2: 报告语言 | 先支持中文模板 |
| Q3: 自动推送渠道 | 列为非目标，后续迭代 |

---

## 13. 附录

### 13.1 配置项清单

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| test_command | 自动检测 | 测试执行命令 |
| result_file | 自动检测 | 解析模式结果文件路径 |
| output_format | markdown | markdown / html / json |
| output_path | reports/ | 报告输出目录 |
| coverage | auto | auto / on / off |
| fail_threshold | 无 | 通过率阈值 |

### 13.2 用户故事映射

| 用户故事 | 功能需求 | 设计模块 |
|----------|----------|----------|
| US1: 本地生成报告 | FR1.3 执行模式 | 执行引擎 |
| US2: 失败用例分析 | FR2.3 失败分析 | 失败详情结构 |
| US3: 质量指标摘要 | FR2.2 结果摘要 | 报告生成器 |
| US4: CI 复用已有结果 | FR1.3 解析模式 | 解析器总线 |

---

**文档结束**