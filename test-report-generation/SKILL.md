---
name: test-report-generation
description: 自动执行测试并生成结构化标准测试报告。支持 Jest、Vitest、pytest、JUnit XML 等主流框架，提供执行模式和解析模式，输出 Markdown/HTML/JSON 格式报告，包含摘要、失败分析、用例明细和覆盖率四大板块。
tags: [testing, reporting, quality, ci]
---

# 测试报告生成 (Test Report Generation)

## Overview

本 Skill 提供一站式测试报告生成能力：自动检测项目测试框架 → 执行测试（或解析已有结果）→ 收集结果 → 生成结构化 Markdown 报告。报告包含摘要、失败分析、用例明细、覆盖率四大板块，落盘于 `reports/` 目录。

## When to Use

### 触发条件
- 用户说"生成测试报告"、"跑测试并出报告"、"生成测试报告"
- 用户说"把这个 junit.xml 转成测试报告"、"解析这个测试结果"
- 用户说"test report"、"run tests and report"
- CI 流程中需要将测试结果标准化为报告

### 不适用场景
- 自动生成或修复测试用例（非本 Skill 职责）
- 在线托管或 Web 化展示报告
- 多次运行结果的趋势对比分析
- Lint、安全扫描等非测试类质量报告聚合

## Process

### Step 1: 确定工作模式

首先判断用户意图，确定使用哪种模式：

**执行模式** — 用户要求运行测试并生成报告：
- 触发词："跑测试"、"运行测试"、"执行测试"、"帮我跑测试并生成报告"
- 行为：先执行测试命令，收集结果，再生成报告

**解析模式** — 用户提供已有结果文件：
- 触发词："解析"、"转换"、"从这个文件生成报告"、指定了具体文件路径
- 行为：跳过测试执行，直接解析用户指定的结果文件

### Step 2: 检测测试框架与命令

按以下优先级自动检测：

**优先级 1：用户显式指定**
- 用户明确说了"用 pytest"、"用 npm test"等，直接使用

**优先级 2：项目配置文件**
- 检查 `package.json` 中的 `scripts.test` 字段
- 检查 `pyproject.toml` 中的 `[tool.pytest.ini_options]` 或 `[project.scripts]`
- 检查 `Cargo.toml` 中的 `[lib]` 测试配置

**优先级 3：框架特征文件**
- `jest.config.*` → Jest
- `vitest.config.*` → Vitest
- `pytest.ini`、`tox.ini`、`setup.cfg` → pytest
- `pom.xml` 含 surefire → Maven/JUnit

**检测命令（按优先级）：**

```bash
# 在项目根目录执行以下检测：
# 1. 检查 package.json 中的 test script
cat package.json 2>/dev/null | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('scripts',{}).get('test',''))" 2>/dev/null

# 2. 检查 jest 配置
ls jest.config.* 2>/dev/null

# 3. 检查 vitest 配置
ls vitest.config.* 2>/dev/null

# 4. 检查 pytest 配置
ls pytest.ini setup.cfg tox.ini 2>/dev/null
```

**框架 → 默认命令映射：**
| 框架 | 默认命令 | 结果产物 |
|------|---------|---------|
| Jest | `npx jest --json --outputFile=test-results.json` | test-results.json |
| Vitest | `npx vitest run --reporter=json --outputFile=test-results.json` | test-results.json |
| pytest | `python -m pytest --junitxml=junit.xml -q` | junit.xml |
| Maven/JUnit | `mvn test` | target/surefire-reports/*.xml |

### Step 3: 执行测试（执行模式）

**执行模式**流程：

1. 使用 `exec` 工具运行检测到的测试命令，添加合适的 reporter 参数生成结构化结果文件

2. **命令构造规则：**
   - Jest：添加 `--json --outputFile=test-results.json` 确保 JSON 输出
   - Vitest：添加 `--reporter=json --outputFile=test-results.json`
   - pytest：添加 `--junitxml=junit.xml` 生成 JUnit XML
   - 通用：优先使用 JUnit XML reporter

3. 运行测试（使用 `run_in_background=true` 处理可能的长耗时）：
   ```bash
   npx jest --json --outputFile=test-results.json 2>&1
   npx vitest run --reporter=json --outputFile=test-results.json 2>&1
   python -m pytest --junitxml=junit.xml -q 2>&1
   ```

4. **错误处理：**
   - 如果测试命令执行失败（退出码非 0 且非测试失败）：
     - 诊断原因：命令不存在、依赖缺失、配置错误
     - 返回明确错误信息，不得生成空报告
   - 如果测试本身有失败用例（退出码为 1 等），属于正常情况，继续收集结果

### Step 4: 收集结果文件

**执行模式**：运行测试后，定位生成的结果文件：
- Jest：`test-results.json`
- Vitest：`test-results.json`
- pytest：`junit.xml`
- 通用：检查 `target/surefire-reports/`、`build/test-results/` 等目录

**解析模式**：直接使用用户指定的文件路径。

**覆盖率文件收集：**
- Jest/Vitest：`coverage/coverage-summary.json`
- pytest：`coverage.xml`（需要 pytest-cov 插件）
- 通用：`coverage/cobertura-coverage.xml`

### Step 5: 解析结果

使用 `skills/test-report-generation/scripts/parse_results.py` 脚本解析结果文件。

```bash
python3 skills/test-report-generation/scripts/parse_results.py \
  --result-file <path> \
  --format <jest|vitest|junit|auto> \
  --coverage-file <path> \
  --output /tmp/parsed_results.json
```

脚本功能：
- 自动识别结果文件格式（JUnit XML、Jest JSON、Vitest JSON）
- 提取用例总数、通过/失败/跳过数、耗时
- 提取失败用例的错误信息和堆栈
- 解析覆盖率数据（如存在）
- 输出统一 JSON 结构到 `/tmp/parsed_results.json`

### Step 6: 生成报告

使用 `skills/test-report-generation/scripts/generate_report.py` 生成 Markdown 报告。

```bash
python3 skills/test-report-generation/scripts/generate_report.py \
  --parsed-file /tmp/parsed_results.json \
  --output <report_path> \
  --format markdown \
  --project-name <name> \
  --command "<test_command>"
```

默认输出路径：`reports/test-report-<YYYYMMDD-HHmmss>.md`

### Step 7: 输出结果摘要

报告生成后，向用户展示：
1. **报告路径**：绝对路径或相对路径
2. **结果摘要**：通过率、失败数、跳过数、总耗时
3. **失败时附关键信息**：最关键的 1~3 条失败用例名和错误信息

## Report Structure（报告结构）

生成的 Markdown 报告包含以下固定章节：

### 1. 报告头 (Report Header)

| 属性 | 值 |
|------|-----|
| 项目 | `<project_name>` |
| 生成时间 | `<YYYY-MM-DD HH:mm:ss>` |
| 执行命令 | `<test_command>` |
| 框架/版本 | `<framework> <version>` |
| 执行环境 | `<node/python version, OS>` |

### 2. 结果摘要 (Summary)

| 指标 | 值 |
|------|-----|
| 用例总数 | `<total>` |
| 通过 | `<passed>` |
| 失败 | `<failed>` |
| 跳过 | `<skipped>` |
| 通过率 | `<pass_rate>%` |
| 总耗时 | `<duration>s` |

结论：✅ 全部通过 / ❌ 存在失败

### 3. 失败用例分析 (Failure Analysis)

每条失败用例包含：用例名、所属文件、错误信息、堆栈关键行（截断至可读长度）

### 4. 用例明细 (Test Details)

按测试文件分组的用例列表与各自耗时，超过 200 条时截断并注明

### 5. 覆盖率 (Coverage)

语句/分支/函数/行覆盖率总表，以及低于阈值的文件清单

### 6. 附录 (Appendix)

原始结果文件路径、生成工具版本

## 解析模式使用示例

当用户提供已有结果文件时：

```bash
python3 skills/test-report-generation/scripts/parse_results.py \
  --result-file junit.xml \
  --format junit \
  --output /tmp/parsed_results.json

python3 skills/test-report-generation/scripts/generate_report.py \
  --parsed-file /tmp/parsed_results.json \
  --output reports/test-report-$(date +%Y%m%d-%H%M%S).md \
  --format markdown
```

## 可配置项

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `test_command` | 自动检测 | 测试执行命令 |
| `result_file` | 自动检测 | 解析模式下的结果文件路径 |
| `output_format` | `markdown` | `markdown` / `html` / `json` |
| `output_path` | `reports/` | 报告输出目录 |
| `coverage` | `auto` | `auto` / `on` / `off` |
| `fail_threshold` | 无 | 通过率低于该值时结论标记为不达标 |

## Common Rationalizations

- ❌ "我只需要跑测试，不需要报告" → 本 Skill 的核心价值在于报告生成，如果只需要跑测试，直接使用测试框架命令即可
- ❌ "测试通过了，不用生成报告" → 报告不仅是失败记录，也是质量追溯和团队沟通的载体
- ❌ "我可以手动收集结果" → 手动收集耗时且易遗漏，自动化是标准做法

## Red Flags

- 如果用户的项目中没有测试框架配置，提示用户先配置测试框架
- 如果测试执行超时（>300s），建议用户使用解析模式或检查测试性能
- 如果结果文件为空或格式异常，不要生成空报告，明确告知用户问题

## Verification

### 验证方法
1. **功能验证**：在含 Jest 测试的项目中运行测试，然后使用解析脚本生成报告，确认报告内容与原始结果一致
2. **格式验证**：检查生成的 Markdown 报告是否包含所有 6 个章节
3. **错误处理验证**：使用空文件或损坏的 XML/JSON 作为输入，确认脚本返回明确错误而非静默失败
4. **覆盖率验证**：在有 coverage 配置的项目中确认覆盖率章节正确渲染

### 示例验证命令
```bash
mkdir -p /tmp/test-report-demo
python3 skills/test-report-generation/scripts/parse_results.py \
  --result-file skills/test-report-generation/scripts/test_data/sample-junit.xml \
  --format junit \
  --output /tmp/parsed_results.json
python3 skills/test-report-generation/scripts/generate_report.py \
  --parsed-file /tmp/parsed_results.json \
  --output /tmp/test-report-demo/report.md \
  --format markdown
```