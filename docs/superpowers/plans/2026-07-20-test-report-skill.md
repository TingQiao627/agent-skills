# Test Report Skill — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建一个可复用的 Skill，在测试执行后自动解析结果并生成结构化 Markdown 测试报告，首期（M1）支持 Jest/Vitest JSON 和 JUnit XML 格式。

**Architecture:** 采用插件式解析器架构，核心调度器 `generate_report.py` 负责编排流程，`parsers/` 目录下每个框架实现独立解析器，统一产出内部数据模型，再由 `reporters/` 渲染为 Markdown。SKILL.md 作为 Agent 入口引导自动检测项目框架并调用对应路径。

**Tech Stack:** Python 3.9+ (脚本层)，Node.js 测试框架 (Jest/Vitest 作为被测目标)，JUnit XML 作为跨语言兜底格式。

---

## Plan Document Header

- **Plan ID:** `2026-07-20-test-report-skill`
- **Author:** AI Agent
- **Created:** 2026-07-20
- **Status:** Ready for Implementation
- **Spec:** `docs/superpowers/specs/2026-07-20-test-report-skill-design.md`
- **Milestone:** M1 — Jest/Vitest JSON + JUnit XML 解析 + Markdown 报告 + 执行/解析双模式

---

## File Structure

```
skills/test-report/
├── SKILL.md                          # Skill 入口，定义触发意图、执行流程、配置项
├── scripts/
│   ├── generate_report.py            # 核心调度器：模式选择 → 解析 → 生成报告
│   ├── parsers/
│   │   ├── __init__.py               # 解析器注册表 + 公共接口
│   │   ├── jest_vitest.py            # Jest/Vitest JSON 解析器
│   │   └── junit_xml.py              # JUnit XML 解析器
│   ├── reporters/
│   │   ├── __init__.py               # 渲染器注册表
│   │   └── markdown.py               # Markdown 报告渲染器
│   └── models.py                     # 内部数据模型 (Report, Summary, Failure, TestCase, Coverage)
└── tests/
    ├── fixtures/
    │   ├── jest-pass.json            # Jest 全通过用例
    │   ├── jest-fail.json            # Jest 含失败用例
    │   ├── vitest-pass.json          # Vitest 全通过用例
    │   ├── vitest-fail.json          # Vitest 含失败用例
    │   ├── junit-pass.xml            # JUnit XML 全通过
    │   ├── junit-fail.xml            # JUnit XML 含失败
    │   └── junit-malformed.xml       # 损坏的 XML 文件
    ├── test_parsers.py               # 解析器单元测试
    ├── test_reporters.py             # 渲染器单元测试
    └── test_integration.py           # 端到端集成测试
```

**设计原则：**
- 每个文件单一职责：解析器只管解析，渲染器只管渲染，调度器只管编排。
- 解析器注册表模式：新增框架只需在 `parsers/__init__.py` 注册，不修改调度器。
- 数据模型统一：所有解析器输出相同的 `models.Report` 结构，渲染器不感知来源格式。

---

## Tasks

### Task 1: 创建 Skill 骨架与 SKILL.md

- [ ] **Task 1.1** — 创建 `skills/test-report/` 目录结构及 `SKILL.md`
  - **Goal:** 建立 Skill 入口，定义触发意图、配置项、执行流程
  - **Files:** `skills/test-report/SKILL.md`
  - **Content:** 按 design doc 4.4 节定义交互约定，包含：
    - 触发意图示例："生成测试报告"、"跑一下测试并出报告"、"把这个 junit.xml 转成测试报告"
    - 配置项表：`test_command`、`result_file`、`output_format`、`output_path`、`coverage`、`fail_threshold`
    - 执行流程：检测项目框架 → 选择模式 → 执行/解析 → 生成报告
    - 引用脚本路径 `scripts/generate_report.py`
  - **Tests:** 无需测试（Markdown 文档）
  - **Dependencies:** 无

### Task 2: 定义内部数据模型

- [ ] **Task 2.1** — 创建 `scripts/models.py`
  - **Goal:** 定义所有解析器共用的内部数据模型，确保类型一致
  - **Files:** `skills/test-report/scripts/models.py`
  - **Content:** 按 design doc 第 6 节数据模型定义：
    - `Report(header: ReportHeader, summary: Summary, failures: list[Failure], suites: list[TestSuite], coverage: Coverage | None, appendix: Appendix)`
    - `ReportHeader(project_name, generated_at, test_command, framework, framework_version, env_summary)`
    - `Summary(total, passed, failed, skipped, pass_rate, duration_secs, conclusion)`
    - `Failure(case_name, file_path, error_message, stack_trace, suite_name)`
    - `TestSuite(name, file_path, test_cases: list[TestCase])`
    - `TestCase(name, status, duration_secs, error_message, stack_trace)`
    - `Coverage(statement_pct, branch_pct, function_pct, line_pct, low_coverage_files: list[CoverageFile])`
    - `CoverageFile(file_path, statement_pct, branch_pct, function_pct, line_pct)`
    - `Appendix(result_files: list[str], tool_version: str)`
  - **Tests:** 无需单独测试（纯数据类定义）
  - **Dependencies:** 无

### Task 3: 实现 JUnit XML 解析器

- [ ] **Task 3.1** — 创建 `scripts/parsers/__init__.py` 注册表
  - **Goal:** 定义解析器注册表 `PARSER_REGISTRY` 和公共接口 `parse(result_file) -> Report`
  - **Files:** `skills/test-report/scripts/parsers/__init__.py`
  - **Content:**
    - `PARSER_REGISTRY: dict[str, Callable]` 按扩展名映射
    - `parse(result_file: str) -> Report` 根据扩展名分发到对应解析器
    - 未识别的格式抛出明确错误
  - **Tests:** 测试文件将随 Task 3.2 一起覆盖
  - **Dependencies:** Task 2 (models.py)

- [ ] **Task 3.2** — 创建 `scripts/parsers/junit_xml.py`
  - **Goal:** 解析 JUnit XML 格式，产出 `Report` 对象
  - **Files:** `skills/test-report/scripts/parsers/junit_xml.py`
  - **Content:**
    - 使用 `xml.etree.ElementTree` 解析 XML
    - 处理标准 JUnit 结构：`<testsuites>` → `<testsuite>` → `<testcase>` + `<failure>` / `<error>` / `<skipped>`
    - 缺失字段降级为 `"未获取"` 或 `0`，不崩溃
    - 堆栈截断至 15 行以保持可读性
    - 字段缺失/格式异常时降级输出，不抛异常
  - **Tests:** `tests/test_parsers.py` 中覆盖 `junit-pass.xml`、`junit-fail.xml`、`junit-malformed.xml`
  - **Dependencies:** Task 2, Task 3.1

- [ ] **Task 3.3** — 创建 JUnit XML 测试 fixtures
  - **Goal:** 准备测试数据
  - **Files:**
    - `skills/test-report/tests/fixtures/junit-pass.xml` — 标准 JUnit 全通过
    - `skills/test-report/tests/fixtures/junit-fail.xml` — 包含 failure 和 error 用例
    - `skills/test-report/tests/fixtures/junit-malformed.xml` — 损坏的 XML（缺少闭合标签）
  - **Tests:** 被 Task 3.2 测试引用
  - **Dependencies:** 无

### Task 4: 实现 Jest/Vitest JSON 解析器

- [ ] **Task 4.1** — 创建 `scripts/parsers/jest_vitest.py`
  - **Goal:** 解析 Jest/Vitest JSON reporter 输出，产出 `Report` 对象
  - **Files:** `skills/test-report/scripts/parsers/jest_vitest.py`
  - **Content:**
    - 解析 `--json` reporter 输出的顶层 JSON 结构
    - Jest 格式：`{ testResults: [{ assertionResults: [...] }] }`
    - Vitest 格式：`{ testResults: [{ testResults: [...] }] }`（注意嵌套差异）
    - 自动检测是 Jest 还是 Vitest 格式（通过检查顶层字段）
    - 缺失字段降级为 `"未获取"` 或 `0`
    - 堆栈截断至 15 行
    - 汇总 `numTotalTests`、`numPassedTests`、`numFailedTests` 等
  - **Tests:** `tests/test_parsers.py` 中覆盖 `jest-pass.json`、`jest-fail.json`、`vitest-pass.json`、`vitest-fail.json`
  - **Dependencies:** Task 2, Task 3.1

- [ ] **Task 4.2** — 创建 Jest/Vitest 测试 fixtures
  - **Goal:** 准备测试数据
  - **Files:**
    - `skills/test-report/tests/fixtures/jest-pass.json` — Jest 全通过
    - `skills/test-report/tests/fixtures/jest-fail.json` — Jest 含失败用例
    - `skills/test-report/tests/fixtures/vitest-pass.json` — Vitest 全通过
    - `skills/test-report/tests/fixtures/vitest-fail.json` — Vitest 含失败用例
  - **Tests:** 被 Task 4.1 测试引用
  - **Dependencies:** 无

### Task 5: 实现解析器单元测试

- [ ] **Task 5.1** — 创建 `tests/test_parsers.py`
  - **Goal:** 覆盖解析器核心逻辑，包括正常路径、失败路径、边界条件
  - **Files:** `skills/test-report/tests/test_parsers.py`
  - **Content:**
    - `test_junit_pass`: 验证全通过 XML 解析后 Summary 正确
    - `test_junit_fail`: 验证失败用例信息完整（错误信息、堆栈、文件路径）
    - `test_junit_malformed`: 验证损坏 XML 返回明确错误，不崩溃
    - `test_jest_pass`: 验证 Jest 全通过 JSON 解析正确
    - `test_jest_fail`: 验证 Jest 失败用例信息完整
    - `test_vitest_pass`: 验证 Vitest 全通过 JSON 解析正确
    - `test_vitest_fail`: 验证 Vitest 失败用例信息完整
    - `test_parser_registry`: 验证注册表根据扩展名正确分发
    - `test_unknown_format`: 验证未知格式抛出明确错误
  - **Tests:** 自包含，被 Task 3.2 和 Task 4.1 的产出驱动
  - **Dependencies:** Task 3.2, Task 4.1

### Task 6: 实现 Markdown 渲染器

- [ ] **Task 6.1** — 创建 `scripts/reporters/__init__.py` 注册表
  - **Goal:** 定义渲染器注册表 `REPORTER_REGISTRY` 和公共接口
  - **Files:** `skills/test-report/scripts/reporters/__init__.py`
  - **Content:**
    - `REPORTER_REGISTRY: dict[str, Callable]` 按格式名映射
    - `render(report: Report, format: str) -> str` 分发到对应渲染器
  - **Tests:** 被 Task 6.3 集成测试覆盖
  - **Dependencies:** Task 2 (models.py)

- [ ] **Task 6.2** — 创建 `scripts/reporters/markdown.py`
  - **Goal:** 将 `Report` 渲染为标准 Markdown 报告
  - **Files:** `skills/test-report/scripts/reporters/markdown.py`
  - **Content:** 按 design doc 4.2 节标准结构渲染：
    1. 报告头：项目名、生成时间、执行命令、框架/版本、执行环境摘要
    2. 结果摘要：用例总数、通过/失败/跳过数、通过率、总耗时、✅/❌ 结论
    3. 失败用例分析：表格列出每条失败用例（用例名、文件、错误信息、堆栈关键行）
    4. 用例明细：按测试文件分组，表格列出用例名、状态、耗时；超过 200 条时截断并注明
    5. 覆盖率：语句/分支/函数/行覆盖率表 + 低于阈值文件清单；无数据时标注"未获取"
    6. 附录：原始结果文件路径、生成工具版本
  - **Tests:** `tests/test_reporters.py` 中覆盖
  - **Dependencies:** Task 2, Task 6.1

- [ ] **Task 6.3** — 创建 `tests/test_reporters.py`
  - **Goal:** 覆盖 Markdown 渲染器各章节正确性
  - **Files:** `skills/test-report/tests/test_reporters.py`
  - **Content:**
    - `test_render_all_pass`: 验证全通过时报告含 ✅ 结论
    - `test_render_with_failures`: 验证失败用例分析章节正确渲染
    - `test_render_no_coverage`: 验证无覆盖率时标注"未获取"
    - `test_render_with_coverage`: 验证覆盖率章节正确渲染
    - `test_render_over_200_cases`: 验证超过 200 条时截断并注明
    - `test_render_empty_suites`: 验证空用例列表时降级输出
  - **Dependencies:** Task 6.2

### Task 7: 实现核心调度器

- [ ] **Task 7.1** — 创建 `scripts/generate_report.py`
  - **Goal:** 实现执行/解析双模式调度，编排完整流程
  - **Files:** `skills/test-report/scripts/generate_report.py`
  - **Content:**
    - CLI 参数解析（argparse）：
      - `--mode execute|parse`（默认 execute）
      - `--test-command`（执行模式指定命令）
      - `--result-file`（解析模式指定结果文件）
      - `--output-format markdown`（默认 markdown）
      - `--output-path reports/`（默认 reports/）
      - `--coverage auto|on|off`（默认 auto）
      - `--fail-threshold`（可选）
    - 执行模式：`subprocess.run()` 执行测试命令，收集结果文件路径
    - 解析模式：跳过执行，直接读取指定结果文件
    - 流程：收集结果 → 调用 `parsers.parse()` → 调用 `reporters.render()` → 写入文件
    - 结果文件写入 `reports/test-report-<YYYYMMDD-HHmmss>.md`
    - 执行失败（命令无法运行）给出明确诊断，不生成空报告
    - 解析失败（格式异常）给出明确错误，不生成空报告
    - 输出路径信息 + 摘要到 stdout
    - 敏感信息过滤：错误堆栈中过滤环境变量值（检测 `KEY=VALUE` 模式）
  - **Tests:** `tests/test_integration.py` 中覆盖
  - **Dependencies:** Task 2, Task 3.1, Task 4.1, Task 6.1

- [ ] **Task 7.2** — 创建 `tests/test_integration.py`
  - **Goal:** 端到端验证完整流程
  - **Files:** `skills/test-report/tests/test_integration.py`
  - **Content:**
    - `test_parse_mode_junit`: 解析模式 → 输入 JUnit XML → 验证输出 MD 文件存在且内容正确
    - `test_parse_mode_jest`: 解析模式 → 输入 Jest JSON → 验证输出 MD
    - `test_parse_mode_malformed`: 解析模式 → 输入损坏文件 → 验证返回错误，不生成空报告
    - `test_execute_mode_mock`: 执行模式 → Mock 测试命令 → 验证调度正确
    - `test_output_path_custom`: 验证自定义输出路径生效
    - `test_fail_threshold`: 验证通过率低于阈值时结论标记为不达标
    - `test_sensitive_filter`: 验证敏感信息过滤
  - **Dependencies:** Task 7.1

### Task 8: 最终验证与文档

- [ ] **Task 8.1** — 运行全部测试并修复问题
  - **Goal:** 确保所有测试通过
  - **Command:** `cd skills/test-report && python -m pytest tests/ -v`
  - **Dependencies:** Task 1-7

- [ ] **Task 8.2** — 在真实 TS 项目中手动验证
  - **Goal:** 在含 Jest/Vitest 的项目中执行 `python scripts/generate_report.py --mode execute`，验证产出报告
  - **AC 映射:**
    - AC1: 报告符合 4.2 结构，摘要与框架原始输出一致
    - AC2: 失败用例分析包含用例名、文件路径、错误信息
    - AC3: JUnit XML 解析模式不触发测试执行
    - AC4: 损坏文件返回明确错误
    - AC5: 覆盖率数据存在时正确呈现，不存在时标注"未获取"
  - **Dependencies:** Task 8.1

---

## Risks

| Risk | Mitigation |
|------|-----------|
| Jest/Vitest JSON reporter 输出格式差异大 | 解析时自动检测格式（通过顶层字段区分），编写两套 fixture 覆盖 |
| JUnit XML 缺少标准 schema，不同工具产物差异大 | 使用宽松解析策略，缺失字段降级为"未获取"，不崩溃 |
| 测试执行耗时不可控 | 调度器使用 `subprocess.run(timeout=...)` 设置超时，超时后给出诊断 |
| 当前仓库无真实 TS 测试项目 | 手动验证阶段可创建最小示例项目或使用已有 fixture |

---

## Verification

- [ ] `cd skills/test-report && python -m pytest tests/ -v` 全部通过
- [ ] 在真实 Jest/Vitest 项目中手动执行 `generate_report.py --mode execute`，报告符合 AC1-AC5
- [ ] 用 JUnit XML 文件执行 `generate_report.py --mode parse --result-file <path>`，报告正确生成
- [ ] 用损坏文件执行，返回明确错误信息而非空报告

---

## Dependencies Between Tasks

```
Task 1 (SKILL.md) ───── 独立 ──────────────────────────────────────┐
Task 2 (models.py) ──── 独立 ──────────────────────────────────┐   │
Task 3.1 (parsers/__init__) ← Task 2                           │   │
Task 3.2 (junit_xml.py) ← Task 2, 3.1                          │   │
Task 3.3 (junit fixtures) ─ 独立  ───→ Task 3.2 测试          │   │
Task 4.1 (jest_vitest.py) ← Task 2, 3.1                        │   │
Task 4.2 (jest fixtures) ── 独立 ───→ Task 4.1 测试           │   │
Task 5.1 (test_parsers.py) ← Task 3.2, 4.1                     │   │
Task 6.1 (reporters/__init__) ← Task 2                          │   │
Task 6.2 (markdown.py) ← Task 2, 6.1                            │   │
Task 6.3 (test_reporters.py) ← Task 6.2                         │   │
Task 7.1 (generate_report.py) ← Task 2, 3.1, 4.1, 6.1          │   │
Task 7.2 (test_integration.py) ← Task 7.1                       │   │
Task 8.1 (全量测试) ← Task 1-7                                  │   │
Task 8.2 (手动验证) ← Task 8.1                                  │   │
```

---

*Plan generated by writing-plans skill. Ready for implementation via superpowers:subagent-driven-development or superpowers:executing-plans.*