---
name: test-report-generation
description: 自动解析测试结果并生成结构化、可读性强的标准测试报告。支持 Jest/Vitest/pytest/JUnit XML，提供执行与解析双模式。
---

# Test Report Generation

## Overview

此 Skill 帮助 Agent 在测试执行后自动解析测试结果并生成标准化测试报告，解决以下痛点：
- 测试结果散落在终端输出、CI 日志或框架原生产物中，需人工收集整理
- 缺乏统一格式的测试报告，跨项目/跨团队沟通成本高
- 失败用例的上下文需人工回溯
- 覆盖率、通过率等质量指标无法沉淀为可追踪的历史数据

**核心能力：**
- 一条指令完成：执行测试 → 收集结果 → 生成报告
- 支持 Jest、Vitest、pytest、JUnit XML（跨语言兜底）
- 执行模式（运行测试）与解析模式（解析已有结果）双模式
- Markdown 默认输出，结构化报告包含摘要、明细、失败分析、覆盖率

## When to Use

**必用场景：**
- 用户请求"生成测试报告"、"跑测试并出报告"
- 用户请求将 JUnit XML / JSON 测试结果转换为可读报告
- CI 流程中需要将测试结果标准化输出

**典型触发意图：**
- "生成测试报告"
- "跑一下测试并出报告"
- "把这个 junit.xml 转成测试报告"
- "解析 pytest 结果并生成报告"

**不适用场景：**
- 仅需运行测试无需报告 → 直接执行测试命令
- 需要测试用例自动生成或修复 → 非 Skill 范围
- 需要趋势对比分析 → 后续迭代

## Process

### Step 1: 确定工作模式

**判断逻辑：**
1. 用户显式指定结果文件路径 → **解析模式**
2. 用户请求"跑测试并出报告" → **执行模式**
3. 用户仅说"生成测试报告" → 检查是否存在结果文件：
   - 存在 `test-results/`、`coverage/`、`junit.xml` 等 → **解析模式**
   - 不存在 → **执行模式**

**输出：** 明确告知用户当前模式（执行/解析）。

---

### Step 2: 识别测试框架与命令（执行模式）/ 定位结果文件（解析模式）

#### 执行模式 - 框架识别优先级

按以下顺序检测：

| 优先级 | 来源 | 检测方式 |
|--------|------|----------|
| 1 | 用户显式指定 | 用户提供 `test_command` 参数 |
| 2 | package.json scripts | 检查 `scripts.test` 字段 |
| 3 | pyproject.toml | 检查 `[tool.pytest]` 或 `pytest.ini` |
| 4 | Cargo.toml | 检查 `[package]` 配置 |
| 5 | 框架特征文件 | `jest.config.*`、`vitest.config.*`、`pytest.ini` |

**首期支持框架（P0）：**
- JavaScript/TypeScript：Jest、Vitest（需 JSON reporter）
- Python：pytest（JUnit XML / JSON report）
- 通用：JUnit XML（跨语言兜底格式）

#### 解析模式 - 结果文件定位

自动检测路径（按优先级）：
1. 用户指定的 `result_file` 参数
2. `test-results/junit.xml`
3. `test-results/test-results.json`
4. `coverage/test-report.json`
5. 项目根目录的 `junit.xml`、`test-results.json`

**输出：** 确认框架类型、测试命令或结果文件路径。

---

### Step 3: 执行测试（仅执行模式）

**执行要求：**
- 使用后台执行（`run_in_background: true`），避免超时
- 设置合理的超时时间（建议 300s-600s）
- 捕获标准输出和错误输出

**框架特定命令：**

| 框架 | 执行命令（含 JSON 输出） |
|------|--------------------------|
| Jest | `npm test -- --json --outputFile=test-results.json` |
| Vitest | `vitest run --reporter=json --outputFile=test-results.json` |
| pytest | `pytest --junitxml=junit.xml --json-report --json-report-file=test-results.json` |
| JUnit (Maven) | `mvn test -Dmaven.test.failure.ignore=true` |
| JUnit (Gradle) | `gradle test --info` |

**失败处理：**
- 命令无法运行（非用例失败）→ 输出明确诊断信息，终止流程
- 用例失败 → 继续流程，报告中标注失败

---

### Step 4: 解析测试结果

#### Jest JSON 格式解析

```json
{
  "success": boolean,
  "numTotalTests": number,
  "numPassedTests": number,
  "numFailedTests": number,
  "numPendingTests": number,
  "testResults": [{
    "assertionResults": [{
      "ancestorTitles": string[],
      "fullName": string,
      "status": "passed" | "failed" | "pending",
      "duration": number,
      "failureMessages": string[]
    }],
    "name": string
  }]
}
```

#### Vitest JSON 格式解析

```json
{
  "testResults": [{
    "name": string,
    "assertionResults": [{
      "fullName": string,
      "status": "passed" | "failed",
      "duration": number,
      "failureMessages": string[]
    }]
  }]
}
```

#### pytest JUnit XML 解析

```xml
<testsuite name="pytest" tests="N" failures="F" errors="E" skipped="S" time="T">
  <testcase classname="..." name="..." time="...">
    <failure message="...">...</failure>
  </testcase>
</testsuite>
```

#### 通用 JUnit XML 解析

标准 JUnit XML 格式，提取：
- `testsuite` 属性：`tests`、`failures`、`errors`、`skipped`、`time`
- `testcase` 属性：`classname`、`name`、`time`
- `failure` / `error` 元素：`message`、内容文本

---

### Step 5: 收集覆盖率数据（可选）

**覆盖率来源：**
- Jest: `coverage/coverage-final.json`
- Vitest: `coverage/coverage-final.json`
- pytest: `coverage.xml` 或 `.coverage` 文件
- JaCoCo: `target/site/jacoco/jacoco.xml`

**覆盖率指标：**
- 语句覆盖率（statements）
- 分支覆盖率（branches）
- 函数覆盖率（functions）
- 行覆盖率（lines）

若覆盖率文件不存在或解析失败，报告中标注"未获取"，其余章节正常。

---

### Step 6: 生成报告

#### 报告结构（必选章节）

```markdown
# 测试报告

## 1. 报告头
- **项目名**：[项目名称]
- **生成时间**：[YYYY-MM-DD HH:mm:ss]
- **执行命令**：[完整命令]
- **框架/版本**：[框架名 + 版本]
- **执行环境**：[OS + Node/Python 版本摘要]

## 2. 结果摘要
- **用例总数**：[N]
- **通过**：[P] ✅
- **失败**：[F] ❌
- **跳过**：[S]
- **通过率**：[P/N * 100%]
- **总耗时**：[X.XXs]
- **整体结论**：✅ 全部通过 / ❌ 存在失败

## 3. 失败用例分析（仅当存在失败时）
### [用例名]
- **所属文件**：[文件路径:行号]
- **错误信息**：
  ```
  [错误信息摘要]
  ```
- **堆栈关键行**：
  ```
  [堆栈关键行，截断至可读长度]
  ```

## 4. 用例明细
> 按测试文件分组展示

### [测试文件名]
| 用例名 | 状态 | 耗时 |
|--------|------|------|
| [name] | ✅/❌/⏭️ | [Xms] |

*（超过 200 条时截断并注明）*

## 5. 覆盖率
| 指标 | 覆盖率 | 状态 |
|------|--------|------|
| 语句 | [X%] | ✅/⚠️ |
| 分支 | [X%] | ✅/⚠️ |
| 函数 | [X%] | ✅/⚠️ |
| 行 | [X%] | ✅/⚠️ |

*低于阈值的文件清单（若有）：*
- [文件路径]: [X%] ⚠️

## 6. 附录
- **原始结果文件**：[路径]
- **生成工具**：test-report-generation v1.0
```

#### 输出路径

- 默认：`reports/test-report-<YYYYMMDD-HHmmss>.md`
- 用户指定：使用用户提供的 `output_path`

---

### Step 7: 返回结果摘要

向用户返回：
```
📊 测试报告已生成：[报告路径]

摘要：
- 用例总数：[N]
- 通过：[P] ✅
- 失败：[F] ❌
- 通过率：[X%]

[失败时] 关键失败原因：
1. [用例名]: [错误信息摘要]
```

---

## Configuration

用户可通过参数覆盖默认行为：

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `test_command` | 自动检测 | 测试执行命令 |
| `result_file` | 自动检测 | 解析模式下的结果文件路径 |
| `output_format` | markdown | markdown / html / json |
| `output_path` | reports/ | 报告输出目录 |
| `coverage` | auto | auto / on / off |
| `fail_threshold` | 无 | 通过率低于该值时报告结论标记为不达标 |

---

## Common Rationalizations

> 以下想法是错误的，必须忽略：

| 错误想法 | 正确做法 |
|----------|----------|
| "这只是跑测试，不需要 Skill" | 必须使用此 Skill 生成标准化报告 |
| "我可以直接输出测试结果" | 必须生成结构化报告文件，而非终端输出 |
| "测试失败了，报告没意义" | 失败用例更需要报告分析，帮助定位问题 |
| "结果文件格式不对，放弃吧" | 格式异常时降级输出，标注"未获取"，不得崩溃 |

---

## Red Flags

执行过程中若出现以下情况，需特别处理：

| 红旗情况 | 处理方式 |
|----------|----------|
| 测试命令无法运行 | 终止流程，输出明确诊断信息（命令、错误、建议） |
| 结果文件损坏/格式异常 | 返回明确错误说明，不生成空报告 |
| 敏感信息泄露（密钥、环境变量） | 过滤错误堆栈中的敏感路径和凭据 |
| 覆盖率文件不存在 | 标注"未获取"，其余章节正常生成 |
| 用例数超过 200 条 | 截断明细，注明"共 N 条，展示前 200 条" |

---

## Verification

报告生成后，验证以下要点：

- [ ] 报告文件已落盘，路径正确
- [ ] 摘要数据与框架原始输出一致（用例数、通过/失败数）
- [ ] 存在失败用例时，失败分析章节包含用例名、文件路径、错误信息
- [ ] 覆盖率数据存在时正确呈现，不存在时标注"未获取"
- [ ] 无敏感信息泄露（环境变量、密钥）
- [ ] 返回给用户的摘要包含报告路径和核心指标

---

## Examples

### Example 1: 执行模式 - Jest 项目

```
用户: 帮我跑测试并生成报告

Agent:
✅ 检测到 Jest 框架（package.json scripts.test）
📦 执行命令：npm test -- --json --outputFile=test-results.json
⏳ 测试运行中...

✅ 测试完成
📊 解析结果：52 用例，48 通过，4 失败
📝 生成报告：reports/test-report-20260714-090500.md

摘要：
- 用例总数：52
- 通过：48 ✅
- 失败：4 ❌
- 通过率：92.3%

关键失败原因：
1. AuthController should validate token: Expected 200, got 401
2. UserService should handle null: TypeError: Cannot read property 'id'
```

### Example 2: 解析模式 - 已有 JUnit XML

```
用户: 把这个 junit.xml 转成测试报告

Agent:
✅ 解析模式
📄 读取文件：junit.xml
📊 解析结果：128 用例，125 通过，3 失败
📝 生成报告：reports/test-report-20260714-091000.md

摘要：
- 用例总数：128
- 通过：125 ✅
- 失败：3 ❌
- 通过率：97.7%
```

---

## Non-Goals

以下内容不在本期范围内：
- 测试用例的自动生成或修复
- 报告的在线托管 / Web 服务化展示
- 多次运行结果的趋势对比分析
- 非 test 类质量报告（lint、安全扫描）的聚合