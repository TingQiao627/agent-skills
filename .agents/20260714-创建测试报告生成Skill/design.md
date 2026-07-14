# 测试报告生成 Skill 系统设计文档

> 文档版本: v1.0  
> 生成时间: 2026-07-14  
> 设计阶段: 概要设计

---

## 1. 概述

### 1.1 设计背景

当前团队在完成测试执行后，测试结果散落在终端输出、CI日志或框架原生产物中，存在以下痛点：
- 测试结果需要人工收集、整理、汇总，耗时且易遗漏
- 缺乏统一格式的测试报告，跨项目/跨团队沟通成本高
- 失败用例的上下文需要人工回溯
- 覆盖率、通过率等质量指标无法沉淀为可追踪的历史数据

### 1.2 设计目标

提供一个 Skill，使 Agent 在执行测试后能够自动解析测试结果并生成结构化、可读性强的标准测试报告。

### 1.3 设计范围

本文档覆盖测试报告生成 Skill 的系统架构、模块设计、接口设计、数据流设计和技术选型。

---

## 2. 系统架构设计

### 2.1 架构概览

```
┌─────────────────────────────────────────────────────────────┐
│                     Test Report Skill                        │
├─────────────────────────────────────────────────────────────┤
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐    │
│  │ Test Executor│──▶│   Parser     │──▶│   Reporter   │    │
│  │   (Optional) │   │   Plugin     │   │   Generator  │    │
│  └──────────────┘   └──────────────┘   └──────────────┘    │
│         │                   │                   │          │
│         ▼                   ▼                   ▼          │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐    │
│  │   Command    │   │    Result    │   │   Output     │    │
│  │   Detector   │   │   Normalizer │   │   Formatter  │    │
│  └──────────────┘   └──────────────┘   └──────────────┘    │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
              ┌───────────────────────────┐
              │   Output Files            │
              │  - Markdown Report        │
              │  - HTML Report (P1)       │
              │  - JSON Data (Optional)   │
              └───────────────────────────┘
```

### 2.2 核心模块

#### 2.2.1 测试执行器 (Test Executor)
- **职责**: 执行测试命令并收集结果
- **输入**: 测试命令或自动检测的命令
- **输出**: 测试执行结果文件
- **特性**: 
  - 支持自动检测项目测试框架
  - 支持自定义测试命令
  - 支持跳过执行（解析模式）

#### 2.2.2 解析器插件 (Parser Plugin)
- **职责**: 解析不同测试框架的结果文件
- **设计模式**: 插件式架构
- **支持格式**:
  - Jest JSON Reporter
  - Vitest JSON Reporter
  - pytest JUnit XML / JSON
  - JUnit XML (通用)

#### 2.2.3 结果归一化器 (Result Normalizer)
- **职责**: 将不同框架的结果归一化为统一数据结构
- **输入**: 框架特定的解析结果
- **输出**: 标准化的测试结果对象

#### 2.2.4 报告生成器 (Report Generator)
- **职责**: 生成结构化的测试报告
- **组成**: 
  - 摘要生成器
  - 失败分析器
  - 覆盖率处理器
  - 明细生成器

#### 2.2.5 输出格式化器 (Output Formatter)
- **职责**: 将报告数据转换为指定格式
- **支持格式**:
  - Markdown (默认)
  - HTML (P1)
  - JSON (可选)

---

## 3. 数据流设计

### 3.1 主数据流

```
用户指令
    │
    ▼
┌─────────────────┐
│  Intent Parser  │ 解析用户意图
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Config Resolver │ 解析配置参数
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────┐
│         Mode Selector               │
│  ┌─────────┐        ┌────────────┐ │
│  │Execute  │        │  Parse Only│ │
│  │ Mode    │        │    Mode    │ │
│  └────┬────┘        └─────┬──────┘ │
└───────┼───────────────────┼────────┘
        │                   │
        ▼                   ▼
┌─────────────────┐  ┌─────────────────┐
│ Test Executor   │  │   File Locator  │
└────────┬────────┘  └────────┬────────┘
         │                    │
         └────────┬───────────┘
                  ▼
         ┌─────────────────┐
         │  Parser Plugin  │ 选择合适的解析器
         └────────┬────────┘
                  │
                  ▼
         ┌─────────────────┐
         │Result Normalizer│ 归一化结果
         └────────┬────────┘
                  │
                  ▼
         ┌─────────────────┐
         │Report Generator │ 生成报告内容
         └────────┬────────┘
                  │
                  ▼
         ┌─────────────────┐
         │Output Formatter │ 格式化输出
         └────────┬────────┘
                  │
                  ▼
            输出报告文件
```

### 3.2 错误处理流

```
测试执行失败
    │
    ▼
┌─────────────────────┐
│ Error Diagnostics   │ 诊断错误类型
└──────────┬──────────┘
           │
           ├─ 命令不存在 ────▶ 明确提示：未找到测试命令
           │
           ├─ 配置缺失 ───────▶ 提示：缺少必要配置，列出缺失项
           │
           ├─ 框架不支持 ────▶ 提示：当前框架不在支持列表，提供支持列表
           │
           └─ 执行超时 ───────▶ 提示：测试执行超时，建议后台执行
```

---

## 4. 数据模型设计

### 4.1 核心数据结构

#### 4.1.1 测试结果 (TestResult)

```typescript
interface TestResult {
  // 元数据
  metadata: {
    projectName: string;
    generatedAt: string;         // ISO 8601
    testCommand: string;
    framework: string;
    frameworkVersion?: string;
    environment: string;         // node version, python version等
  };
  
  // 摘要统计
  summary: {
    total: number;
    passed: number;
    failed: number;
    skipped: number;
    duration: number;            // 毫秒
    passRate: number;            // 百分比
    status: 'passed' | 'failed';
  };
  
  // 失败用例详情
  failures: FailureCase[];
  
  // 用例明细
  testCases: TestCaseGroup[];    // 按文件分组
  
  // 覆盖率数据
  coverage?: CoverageData;
  
  // 附录信息
  appendix: {
    resultFilePath: string;
    generatorVersion: string;
  };
}
```

#### 4.1.2 失败用例 (FailureCase)

```typescript
interface FailureCase {
  name: string;
  file: string;
  error: {
    message: string;
    stack?: string;              // 截断后的堆栈
  };
  duration?: number;
}
```

#### 4.1.3 测试用例分组 (TestCaseGroup)

```typescript
interface TestCaseGroup {
  file: string;
  cases: TestCase[];
  duration: number;
}

interface TestCase {
  name: string;
  status: 'passed' | 'failed' | 'skipped';
  duration: number;
}
```

#### 4.1.4 覆盖率数据 (CoverageData)

```typescript
interface CoverageData {
  summary: {
    statements: number;          // 百分比
    branches: number;
    functions: number;
    lines: number;
  };
  lowCoverageFiles?: Array<{
    file: string;
    coverage: CoverageSummary;
  }>;
}
```

### 4.2 配置模型

```typescript
interface ReportConfig {
  testCommand?: string;          // 自动检测或用户指定
  resultFile?: string;           // 解析模式必填
  outputFormat: 'markdown' | 'html' | 'json';
  outputPath: string;            // 默认 reports/
  coverage: 'auto' | 'on' | 'off';
  failThreshold?: number;        // 通过率阈值
}
```

---

## 5. 解析器插件设计

### 5.1 插件接口

```typescript
interface TestParserPlugin {
  name: string;
  supportedFormats: string[];
  
  // 检测是否支持该文件格式
  canParse(filePath: string, content?: string): boolean;
  
  // 解析测试结果
  parse(filePath: string): Promise<TestResult>;
}
```

### 5.2 插件注册机制

```typescript
class ParserRegistry {
  private plugins: TestParserPlugin[] = [];
  
  register(plugin: TestParserPlugin): void {
    this.plugins.push(plugin);
  }
  
  findParser(filePath: string): TestParserPlugin | null {
    return this.plugins.find(p => p.canParse(filePath)) || null;
  }
}
```

### 5.3 首期支持的解析器

| 解析器 | 文件格式 | 优先级 | 状态 |
|--------|---------|--------|------|
| JestParser | Jest JSON Reporter | P0 | 待实现 |
| VitestParser | Vitest JSON Reporter | P0 | 待实现 |
| PytestParser | pytest JUnit XML/JSON | P1 | 待实现 |
| JUnitParser | JUnit XML | P0 | 待实现 |

---

## 6. 报告结构设计

### 6.1 Markdown 报告结构

```markdown
# 测试报告 - {项目名}

> 生成时间: {时间戳}  
> 测试框架: {框架名} v{版本}  
> 执行命令: `{命令}`  
> 执行环境: {环境信息}

---

## 📊 结果摘要

| 指标 | 数值 |
|------|------|
| 用例总数 | {total} |
| ✅ 通过 | {passed} |
| ❌ 失败 | {failed} |
| ⏭️ 跳过 | {skipped} |
| 通过率 | {passRate}% |
| 总耗时 | {duration}s |

**整体结论**: ✅ 通过 / ❌ 未通过

---

## ❌ 失败用例分析

### 1. {用例名}

- **文件**: `{文件路径}`
- **错误信息**:
  ```
  {错误消息}
  ```
- **堆栈摘要**:
  ```
  {关键堆栈行}
  ```

---

## 📋 用例明细

### {测试文件1}
| 用例名 | 状态 | 耗时 |
|--------|------|------|
| test_case_1 | ✅ | 10ms |
| test_case_2 | ❌ | 5ms |

### {测试文件2}
...

---

## 📈 覆盖率

| 类型 | 覆盖率 |
|------|--------|
| 语句 | {statements}% |
| 分支 | {branches}% |
| 函数 | {functions}% |
| 行 | {lines}% |

### 低覆盖率文件
| 文件 | 覆盖率 |
|------|--------|
| {file} | {coverage}% |

---

## 📎 附录

- 原始结果文件: `{路径}`
- 生成工具版本: {version}
```

---

## 7. 技术选型

### 7.1 技术栈

| 组件 | 技术选型 | 理由 |
|------|---------|------|
| Skill 框架 | Claude Skill SDK | 项目标准 |
| 解析器 | TypeScript | 类型安全、插件友好 |
| Markdown 生成 | 模板字符串 | 简单可控 |
| HTML 生成 | 模板引擎 (P1) | 灵活性 |
| 文件操作 | Node.js fs | 原生支持 |

### 7.2 依赖库

| 依赖 | 用途 | 版本要求 |
|------|------|----------|
| fast-xml-parser | JUnit XML 解析 | ^4.x |
| glob | 文件匹配 | ^10.x |
| chalk | 终端输出美化 | ^5.x |

---

## 8. 接口设计

### 8.1 Skill 触发接口

```typescript
interface SkillInput {
  // 意图识别
  intent: 'generate_test_report' | 'parse_test_results';
  
  // 配置参数（可选）
  config?: Partial<ReportConfig>;
  
  // 显式指定的结果文件（解析模式）
  resultFile?: string;
}
```

### 8.2 Skill 输出接口

```typescript
interface SkillOutput {
  success: boolean;
  
  // 成功时
  reportPath?: string;
  summary?: {
    total: number;
    passed: number;
    failed: number;
    passRate: number;
  };
  criticalFailures?: string[];  // 关键失败原因（最多3条）
  
  // 失败时
  error?: {
    code: string;
    message: string;
    details?: string;
  };
}
```

---

## 9. 实现计划

### 9.1 里程碑规划

| 阶段 | 内容 | 预计工作量 | 优先级 |
|------|------|-----------|--------|
| M1 | Jest/Vitest JSON + JUnit XML 解析、Markdown 报告、执行/解析双模式 | 5天 | P0 |
| M2 | pytest 支持、覆盖率章节、fail_threshold | 3天 | P1 |
| M3 | HTML 输出、JSON 伴随产物 | 2天 | P1 |
| M4 | 历史趋势对比、更多框架 | 后续迭代 | P2 |

### 9.2 文件结构规划

```
skills/test-report-generator/
├── SKILL.md                    # Skill 定义
├── scripts/
│   ├── index.ts                # 入口
│   ├── parser/
│   │   ├── base.ts             # 解析器基类
│   │   ├── jest.ts             # Jest 解析器
│   │   ├── vitest.ts           # Vitest 解析器
│   │   ├── pytest.ts           # pytest 解析器
│   │   └── junit.ts            # JUnit 解析器
│   ├── normalizer/
│   │   └── index.ts            # 结果归一化
│   ├── generator/
│   │   ├── index.ts            # 报告生成器
│   │   ├── markdown.ts         # Markdown 格式化
│   │   └── html.ts             # HTML 格式化 (P1)
│   └── detector/
│       └── index.ts            # 框架检测
└── templates/
    ├── report.md.ejs           # Markdown 模板
    └── report.html.ejs         # HTML 模板 (P1)
```

---

## 10. 验收清单

### 10.1 功能验收

| 编号 | 验收项 | 验收标准 |
|------|--------|---------|
| AC1 | Jest/Vitest 项目报告生成 | 执行"生成测试报告"产出符合结构的 Markdown 报告，数据一致 |
| AC2 | 失败用例分析 | 报告包含用例名、文件路径、错误信息 |
| AC3 | 解析模式 | 提供 JUnit XML 时不触发测试执行即可产出报告 |
| AC4 | 错误处理 | 结果文件损坏时返回明确错误说明 |
| AC5 | 覆盖率 | 数据存在时正确呈现，不存在时标注"未获取" |

### 10.2 非功能验收

| 编号 | 验收项 | 验收标准 |
|------|--------|---------|
| NFR1 | 性能 | 1000 用例结果解析+生成 < 5s |
| NFR2 | 健壮性 | 格式异常时降级输出，不崩溃 |
| NFR3 | 安全 | 不泄露环境变量/密钥 |
| NFR4 | 幂等性 | 同一结果多次生成内容一致（时间戳除外） |

---

## 11. 风险与对策

### 11.1 技术风险

| 风险 | 影响 | 对策 |
|------|------|------|
| R1: 各框架输出差异大 | 解析复杂度增加 | 插件式架构隔离变化 |
| R2: 长时间测试执行 | 用户体验下降 | 支持后台执行+轮询 |
| R3: 覆盖率格式不统一 | 数据归一化困难 | 按框架分别处理 |

### 11.2 开放问题

| 问题 | 状态 | 决策建议 |
|------|------|---------|
| Q1: 目标项目栈是否以 TS/Node 为主？ | 待确认 | 假设 TS/Node 为主制定 P0 范围 |
| Q2: 报告是否需要双语模板？ | 待确认 | 默认中文模板，后期可扩展 |
| Q3: 是否需要自动推送到 IM？ | 待确认 | 列为非目标，后续迭代 |

---

## 12. 附录

### 12.1 参考文档

- Jest JSON Reporter: https://jestjs.io/docs/configuration#testresultsprocessor-string
- Vitest JSON Reporter: https://vitest.dev/config/#reporters
- pytest JUnit XML: https://docs.pytest.org/en/stable/how-to/output.html#creating-junitxml-format-files
- JUnit XML Schema: https://github.com/windyroad/JUnit.xsd

### 12.2 术语表

| 术语 | 定义 |
|------|------|
| Skill | Claude Code 的技能扩展机制 |
| Parser Plugin | 测试结果解析插件 |
| 解析模式 | 仅解析已有结果文件，不执行测试 |
| 执行模式 | 先执行测试，再生成报告 |

---

> 文档结束