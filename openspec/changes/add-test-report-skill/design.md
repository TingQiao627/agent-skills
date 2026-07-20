# Design: 测试报告生成 Skill

## Architecture Overview

```
┌─────────────────────────────────────────────────┐
│                  Skill Entry                      │
│         (test-report-generator)                   │
├─────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────────────────┐  │
│  │  Mode Router  │  │  Config Resolver          │  │
│  │  exec/parse   │  │  (test_command, output..) │  │
│  └──────┬───────┘  └──────────────────────────┘  │
│         │                                         │
│  ┌──────▼───────┐  ┌──────────────────────────┐  │
│  │  Test Runner  │  │  Framework Detector       │  │
│  │  (exec mode)  │  │  - jest / vitest / pytest │  │
│  └──────┬───────┘  └──────────────────────────┘  │
│         │                                         │
│  ┌──────▼──────────────────────────────────────┐ │
│  │           Parser Registry (Plugin)            │ │
│  │  ┌──────────┐ ┌──────────┐ ┌────────────┐  │ │
│  │  │ Jest JSON│ │Vitest JSON│ │JUnit XML   │  │ │
│  │  │ Parser   │ │Parser    │ │Parser      │  │ │
│  │  └──────────┘ └──────────┘ └────────────┘  │ │
│  └──────┬──────────────────────────────────────┘ │
│         │                                         │
│  ┌──────▼───────┐  ┌──────────────────────────┐  │
│  │  Report       │  │  Coverage Collector      │  │
│  │  Builder      │  │  (istanbul / pytest-cov) │  │
│  └──────┬───────┘  └──────────────────────────┘  │
│         │                                         │
│  ┌──────▼───────┐                                │
│  │  Output       │                                │
│  │  Writer (MD)  │                                │
│  └──────────────┘                                │
└─────────────────────────────────────────────────┘
```

## Parser Plugin Design

### Plugin Interface

每个解析器实现统一接口：

```typescript
interface TestResultParser {
  /** 框架标识，如 'jest', 'vitest', 'pytest', 'junit' */
  readonly framework: string;
  /** 接受的文件扩展名列表 */
  readonly supportedExtensions: string[];
  /** 解析结果文件，返回标准化结果 */
  parse(filePath: string): ParsedTestResult;
  /** 检测该文件是否可由本解析器处理 */
  canParse(filePath: string): boolean;
}
```

### Normalized Result Model

```typescript
interface ParsedTestResult {
  summary: {
    total: number;
    passed: number;
    failed: number;
    skipped: number;
    passRate: number;
    totalDurationMs: number;
    overallPassed: boolean;
  };
  suites: TestSuite[];
  coverage?: CoverageData;
  framework: { name: string; version?: string };
  rawResultPath: string;
}

interface TestSuite {
  file: string;
  cases: TestCase[];
}

interface TestCase {
  name: string;
  status: 'passed' | 'failed' | 'skipped';
  durationMs: number;
  failure?: {
    message: string;
    stack: string;       // truncated to readable length
    sourceFile?: string;
    sourceLine?: number;
  };
}
```

### Framework Detection Priority

1. 用户显式指定的命令（最高优先级）
2. `package.json` scripts（`test`）、`pyproject.toml`、`Cargo.toml` 等
3. 框架特征文件推断（`jest.config.*`、`vitest.config.*`、`pytest.ini`）

## Report Structure

生成的 Markdown 报告固定包含以下章节：

1. **报告头**：项目名、生成时间、执行命令、框架/版本、执行环境摘要
2. **结果摘要**：用例总数、通过/失败/跳过数、通过率、总耗时；整体结论 ✅/❌
3. **失败用例分析**：用例名、所属文件、错误信息、堆栈关键行（截断）
4. **用例明细**：按测试文件分组的用例列表与耗时，超过 200 条时截断并注明
5. **覆盖率**：语句/分支/函数/行覆盖率总表，低于阈值的文件清单
6. **附录**：原始结果文件路径、生成工具版本

## Output Format

| 格式 | 优先级 | 说明 |
|------|--------|------|
| Markdown (`.md`) | P0 | 默认格式 |
| HTML (`.html`) | P1 | 后续迭代 |
| JSON (`.json`) | P1 | 结构化伴随产物 |

默认输出路径：`reports/test-report-<YYYYMMDD-HHmmss>.md`

## Configuration

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `test_command` | 自动检测 | 测试执行命令 |
| `result_file` | 自动检测 | 解析模式下的结果文件路径 |
| `output_format` | `markdown` | `markdown` / `html` / `json` |
| `output_path` | `reports/` | 报告输出目录 |
| `coverage` | `auto` | `auto` / `on` / `off` |
| `fail_threshold` | 无 | 通过率低于该值时报告结论标记为不达标 |

## Skill File Layout

```
skills/test-report-generator/
├── SKILL.md                  # Skill 入口与工作流
├── scripts/
│   ├── detect-framework.ts   # 框架检测
│   ├── run-test.ts           # 测试执行
│   ├── parsers/
│   │   ├── jest-parser.ts    # Jest JSON 解析器
│   │   ├── vitest-parser.ts  # Vitest JSON 解析器
│   │   ├── pytest-parser.ts  # pytest JUnit XML / JSON 解析器
│   │   └── junit-parser.ts   # 通用 JUnit XML 解析器
│   ├── report-builder.ts     # 报告构建器
│   ├── md-writer.ts          # Markdown 输出
│   └── coverage-collector.ts # 覆盖率采集
└── templates/
    └── report-template.md    # Markdown 报告模板
```

## Milestones

| 阶段 | 范围 | 优先级 |
|------|------|--------|
| M1 | Jest/Vitest JSON + JUnit XML 解析、Markdown 报告、执行/解析双模式 | P0 |
| M2 | pytest 支持、覆盖率章节、fail_threshold | P1 |
| M3 | HTML 输出、JSON 伴随产物 | P1 |
| M4 | 历史趋势对比、更多框架（Go test / cargo test） | P2（后续迭代） |