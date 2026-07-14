---
name: test-report-generation
description: 自动解析测试结果并生成结构化测试报告。支持 Jest/Vitest/pytest/JUnit XML，输出 Markdown/HTML/JSON 格式。使用场景：执行测试后生成报告、解析已有测试结果文件。
---

# 测试报告生成

## Overview

在测试执行后自动解析测试结果并生成结构化、可读性强的标准测试报告。支持主流测试框架（Jest、Vitest、pytest），统一报告格式，包含摘要、失败分析、用例明细、覆盖率等板块。

## When to Use

- 用户说「生成测试报告」「跑一下测试并出报告」
- 用户说「把这个 junit.xml 转成测试报告」
- CI 流程需要将已有测试结果转成报告
- 需要向团队同步测试质量状态（通过率、覆盖率）

**When NOT to use:** 生成测试用例、修复测试失败、非测试类质量报告（lint/安全扫描）。

## Process

### Step 1: 确定工作模式

```
IF 用户指定结果文件路径 (result_file 参数):
  → 解析模式：跳过测试执行，直接解析已有结果文件
ELSE:
  → 执行模式：触发测试运行并收集结果
```

### Step 2: 框架检测（仅执行模式）

**检测优先级：**
1. 用户显式指定命令 (`test_command` 参数)
2. 项目配置文件检测：
   - `package.json` scripts.test → Jest/Vitest
   - `pyproject.toml` [tool.pytest] → pytest
   - `pytest.ini` / `setup.cfg` → pytest
3. 框架特征文件推断：
   - `jest.config.*` → Jest
   - `vitest.config.*` → Vitest
   - `pytest.ini` → pytest
   - 无匹配 → JUnit XML 兜底

**Action:** 调用 `scripts/utils/framework-detector.js` 检测框架并返回默认测试命令。

### Step 3: 执行测试（仅执行模式）

根据检测到的框架，执行对应命令并收集 JSON/XML 结果：

| 框架 | 执行命令 | 输出格式 |
|------|---------|---------|
| Jest | `npm test -- --json --outputFile=<temp>` | JSON |
| Vitest | `vitest --reporter=json --outputFile=<temp>` | JSON |
| pytest | `pytest --junit-xml=<temp>` | JUnit XML |

**错误处理：**
- 测试命令无法运行：输出诊断信息（命令、退出码、stderr），**终止流程**，不生成空报告
- 测试执行超时（默认 10 分钟）：返回超时错误

### Step 4: 解析测试结果

调用对应解析器：

| 框架 | 解析器脚本 |
|------|-----------|
| Jest/Vitest | `scripts/parsers/jest-vitest.js` |
| pytest (JUnit XML) | `scripts/parsers/junit-xml.js` |
| 通用 JUnit XML | `scripts/parsers/junit-xml.js` |

**解析接口：** `parse(resultFile) → TestResult`

**TestResult 结构：**
```javascript
{
  framework: string,       // 'jest' | 'vitest' | 'pytest' | 'junit'
  version: string,         // 框架版本
  total: number,           // 用例总数
  passed: number,          // 通过数
  failed: number,          // 失败数
  skipped: number,         // 跳过数
  duration: number,        // 总耗时（毫秒）
  passRate: number,        // 通过率（百分比）
  testFiles: [{            // 测试文件列表
    path: string,          // 文件路径
    tests: [{              // 用例列表
      name: string,        // 用例名
      status: 'passed' | 'failed' | 'skipped',
      duration: number,    // 耗时（毫秒）
      error?: {            // 失败时必选
        message: string,   // 错误信息
        stack: string[]    // 堆栈关键行（截断至 5 行）
      }
    }]
  }],
  coverage?: {             // 覆盖率（若可获取）
    statements: number,
    branches: number,
    functions: number,
    lines: number,
    lowCoverageFiles: [{   // 低于阈值文件
      path: string,
      coverage: number
    }]
  }
}
```

**降级处理：**
- 结果文件字段缺失：缺失项标注「未获取」，继续解析
- 解析器异常：捕获错误，输出错误位置和原因，**终止流程**

### Step 5: 敏感信息过滤

调用 `scripts/utils/sanitizer.js` 过滤敏感信息：
- 环境变量（`process.env.*`, `os.environ.*`）
- 密钥类内容（API_KEY, TOKEN, SECRET）
- 敏感路径（`/home/{user}`, `/Users/{user}`）

**Action:** 在生成报告前对 TestResult 执行清洗。

### Step 6: 生成报告

调用对应生成器：

| 格式 | 生成器脚本 | 输出 |
|------|-----------|------|
| Markdown | `scripts/generators/markdown.js` | `.md` 文件 |
| HTML | `scripts/generators/html.js` | `.html` 文件（P1）|
| JSON | `scripts/generators/markdown.js`（伴随） | `.json` 文件 |

**默认输出路径：** `reports/test-report-<YYYYMMDD-HHmmss>.md`

**报告结构：**
1. 报告头：项目名、生成时间、执行命令、框架/版本、执行环境
2. 结果摘要：用例总数、通过/失败/跳过数、通过率、总耗时、整体结论（✅/❌）
3. 失败用例分析：用例名、文件路径、错误信息、堆栈关键行
4. 用例明细：按文件分组的用例列表与耗时（超过 200 条截断）
5. 覆盖率：语句/分支/函数/行覆盖率、低于阈值文件清单
6. 附录：原始结果文件路径、生成工具版本

### Step 7: 输出结果

返回：
- 报告路径
- 结果摘要（通过率、失败数）
- 失败时附带最关键的 1~3 条失败原因

## Configuration

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| test_command | string | 自动检测 | 测试执行命令 |
| result_file | string | 自动检测 | 解析模式结果文件路径 |
| output_format | enum | markdown | markdown / html / json |
| output_path | string | reports/ | 报告输出目录 |
| coverage | enum | auto | auto / on / off |
| fail_threshold | number | null | 通过率阈值（低于则标记不达标） |

## Common Rationalizations

> "这个项目没有测试，可以直接跳过。"
**错误。** 应明确告知用户无测试可执行，不要静默跳过。

> "测试失败了，报告没意义。"
**错误。** 失败时更需要报告来定位问题，必须生成失败分析章节。

> "覆盖率数据不完整，干脆不展示。"
**错误。** 应标注「未获取」或展示已有数据，其余章节正常输出。

## Red Flags

- 测试执行失败时生成空报告或报告显示「全部通过」
- 报告中包含环境变量、密钥等敏感信息
- 用例数与原始测试结果不一致
- 失败用例缺少错误信息或文件路径
- 解析异常时静默丢弃数据而非报错

## Verification

**AC1（Jest/Vitest 执行模式）：**
- 自动检测框架
- 执行测试并收集 JSON 结果
- 报告结构与规范一致
- 摘要数据与原始输出一致

**AC2（失败用例分析）：**
- 报告第 3 章节包含失败用例
- 每条包含：用例名、文件路径、错误信息
- 堆栈截断至可读长度

**AC3（JUnit XML 解析模式）：**
- 不触发测试执行
- 直接解析 JUnit XML
- 产出完整报告

**AC4（错误文件处理）：**
- 损坏文件返回明确错误
- 不生成空报告

**AC5（覆盖率降级）：**
- 无覆盖率数据时标注「未获取」
- 其余章节正常输出

## Examples

### 示例 1：执行模式生成报告

```bash
# 用户：生成测试报告

# Agent 执行：
# 1. 检测到 package.json 含 jest.config.js → Jest
# 2. 执行 npm test -- --json --outputFile=/tmp/test-results.json
# 3. 解析 JSON 结果
# 4. 生成 reports/test-report-20260714-093000.md
# 5. 输出摘要：
#    ✓ 报告已生成：reports/test-report-20260714-093000.md
#    ✓ 通过率：95.5%（191/200）
#    ✗ 失败数：9
#    关键失败：
#    1. UserService.createUser - 用户名长度校验失败
#    2. AuthService.login - Token 验证异常
#    3. TaskService.complete - 状态转换错误
```

### 示例 2：解析模式转换已有结果

```bash
# 用户：把这个 test-results.xml 转成测试报告

# Agent 执行：
# 1. 检测文件格式为 JUnit XML
# 2. 解析 XML
# 3. 生成 reports/test-report-20260714-093500.md
# 4. 输出摘要：
#    ✓ 报告已生成：reports/test-report-20260714-093500.md
#    ✓ 通过率：100%（50/50）
```