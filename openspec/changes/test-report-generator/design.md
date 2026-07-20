# Design: test-report-generator

## Architecture Overview

test-report-generator 是一个独立 Skill，位于 `skills/test-report-generator/`。采用**解析器插件式架构**，核心流程分为三个阶段：

```
[配置解析] → [数据采集] → [报告生成]
     │              │              │
     ├─ 用户配置     ├─ 执行模式     ├─ Markdown
     ├─ 项目检测     ├─ 解析模式     ├─ HTML (P1)
     └─ 默认值       └─ 解析器插件    └─ JSON (P1)
```

## Component Design

### 1. Skill 入口 (`SKILL.md`)
- 定义 Skill 的触发条件、工作流程、配置项
- 引用 `scripts/` 下的执行脚本

### 2. Config Resolver (`scripts/config.py`)
- 解析用户配置（覆盖默认值）
- 自动检测项目测试框架
- 检测优先级：用户指定 > 项目配置 > 特征文件推断

### 3. Test Runner (`scripts/runner.py`)
- 执行模式：调用测试命令并捕获输出
- 解析模式：跳过执行，直接读取结果文件
- 后台执行支持（长任务轮询）

### 4. Parser Registry (`scripts/parsers/`)
插件式解析器结构：
```
scripts/parsers/
├── __init__.py          # 解析器注册表
├── base.py              # 抽象基类 ParserBase
├── jest_parser.py       # Jest JSON 解析
├── vitest_parser.py     # Vitest JSON 解析
├── pytest_parser.py     # pytest JUnit XML / JSON 解析
└── junit_parser.py      # 通用 JUnit XML 解析
```

#### ParserBase 接口
```python
class ParserBase:
    @staticmethod
    def can_parse(file_path: str) -> bool: ...
    def parse(self, file_path: str) -> TestResult: ...
```

### 5. Report Generator (`scripts/reporter.py`)
- 接收 `TestResult` 数据模型
- 根据 `output_format` 生成对应格式报告
- 默认 Markdown 模板

### 6. Coverage Collector (`scripts/coverage.py`)
- 三种模式：`auto`（自动检测）、`on`（强制收集）、`off`（跳过）
- 支持常见覆盖率工具输出

## Data Model

### TestResult
```python
@dataclass
class TestResult:
    project_name: str
    timestamp: str
    command: str
    framework: str
    framework_version: str
    summary: TestSummary
    suites: list[TestSuite]
    coverage: CoverageData | None
    errors: list[str]          # 解析错误（降级输出用）

@dataclass
class TestSummary:
    total: int
    passed: int
    failed: int
    skipped: int
    pass_rate: float
    duration_secs: float
    conclusion: str            # "pass" | "fail"

@dataclass
class TestSuite:
    file_path: str
    name: str
    cases: list[TestCase]

@dataclass
class TestCase:
    name: str
    file_path: str
    status: str                # "passed" | "failed" | "skipped"
    duration_secs: float | None
    error_message: str | None
    error_stack: str | None

@dataclass
class CoverageData:
    statements: float | None
    branches: float | None
    functions: float | None
    lines: float | None
    low_coverage_files: list[tuple[str, float]]  # (file, pct)
```

## Report Template (Markdown)

```markdown
# 测试报告：<project_name>

> 生成时间：<timestamp>
> 执行命令：<command>
> 框架：<framework> <version>
> 环境：<env_summary>

---

## 结果摘要

| 指标 | 数值 |
|------|------|
| 用例总数 | <total> |
| 通过 | <passed> |
| 失败 | <failed> |
| 跳过 | <skipped> |
| 通过率 | <pass_rate>% |
| 总耗时 | <duration>s |
| 结论 | ✅ / ❌ |

---

## 失败用例分析

> 仅在有失败用例时出现。

### <case_name>
- **文件**：<file_path>
- **错误信息**：<error_message>
- **堆栈**：
  ```
  <truncated_stack>
  ```

---

## 用例明细

### <file_path>
| 用例 | 状态 | 耗时 |
|------|------|------|
| <name> | ✅/❌/⏭️ | <duration>s |

---

## 覆盖率

| 类型 | 覆盖率 |
|------|--------|
| 语句 | <statements>% |
| 分支 | <branches>% |
| 函数 | <functions>% |
| 行 | <lines>% |

> 低于阈值的文件：
> - <file>: <pct>%

---

## 附录

- 原始结果文件：<result_file_path>
- 生成工具：test-report-generator v<version>
```

## Key Decisions

1. **Python 实现**：Skill 脚本使用 Python 实现，确保跨平台兼容性和解析灵活性
2. **插件式解析器**：每个框架独立解析器文件，通过 `ParserBase` 抽象基类注册，符合 NFR5
3. **降级策略**：解析失败不崩溃，字段缺失标注"未获取"，符合 NFR2
4. **Markdown 优先**：默认输出 Markdown，HTML/JSON 为 P1 扩展，降低首期复杂度
5. **配置覆盖链**：用户显式配置 > 项目配置检测 > 默认值

## Open Questions
- 报告的模板语言（中文/英文）待需求方确认，当前按中文模板设计
- 是否需要在 Skill 中内置 IM/邮件推送能力，当前列为非目标