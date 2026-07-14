---
name: test-report-generator
description: 自动解析测试结果并生成结构化测试报告。当用户说"生成测试报告"、"跑测试出报告"、"解析 junit.xml"时使用。支持 Jest/Vitest/pytest/JUnit XML 解析，输出 Markdown/HTML/JSON 格式报告。
---

# 测试报告生成器

## Overview

自动执行测试、解析测试结果、生成结构化可读性强的标准测试报告。解决测试结果散落在各处、需人工收集整理、缺乏统一格式等痛点。

## When to Use

**触发场景：**
- 用户说："生成测试报告"、"跑一下测试并出报告"、"把这个 junit.xml 转成测试报告"
- 测试执行后需要汇总结果
- CI 流程中需要将已有测试结果转成报告

**NOT to use：**
- 仅需运行测试（使用 `test-driven-development`）
- 需要生成测试用例（本 Skill 仅报告，不生成用例）

## Process

### Step 1: 意图识别与模式选择

识别用户意图，选择工作模式：

```
意图关键词 → 模式
- "生成测试报告" / "跑测试出报告" → 执行模式
- "解析 junit.xml" / "转成报告" → 解析模式
```

**执行模式**：触发测试运行 → 收集结果 → 生成报告
**解析模式**：跳过执行 → 直接解析用户指定的结果文件

### Step 2: 配置解析

提取用户配置（用户可覆盖默认值）：

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `test_command` | 自动检测 | 测试执行命令 |
| `result_file` | 自动检测 | 解析模式下的结果文件路径 |
| `output_format` | `markdown` | `markdown` / `html` / `json` |
| `output_path` | `reports/` | 报告输出目录 |
| `coverage` | `auto` | `auto` / `on` / `off` |
| `fail_threshold` | 无 | 通过率阈值（可选） |

**配置来源优先级**：
1. 用户显式指定
2. 项目配置文件（package.json scripts、pyproject.toml、Cargo.toml）
3. 框架特征文件推断（jest.config.*、vitest.config.*、pytest.ini）

### Step 3: 测试框架检测与执行（仅执行模式）

**检测优先级**：
1. 用户指定的 `test_command`
2. `package.json` → `npm test` / `npm run test`
3. `pyproject.toml` → `pytest`
4. `Cargo.toml` → `cargo test`
5. 特征文件：`jest.config.*` / `vitest.config.*` / `pytest.ini`

**执行测试**：
```bash
# Jest/Vitest - 生成 JSON 报告
npm test -- --json --outputFile=test-results.json

# pytest - 生成 JUnit XML
pytest --junitxml=test-results.xml

# JUnit 格式兜底
# 直接运行框架默认命令
```

**错误处理**：
- 命令不存在 → 明确提示："未找到测试命令，请手动指定 `test_command`"
- 配置缺失 → 列出缺失项
- 执行超时 → 提示建议后台执行，不生成空报告

### Step 4: 选择解析器插件

根据结果文件格式选择解析器：

| 文件格式 | 解析器 | 支持状态 |
|---------|--------|---------|
| Jest JSON Reporter | `JestParser` | P0 ✅ |
| Vitest JSON Reporter | `VitestParser` | P0 ✅ |
| pytest JUnit XML | `PytestParser` | P1 ✅ |
| JUnit XML (通用) | `JUnitParser` | P0 ✅ |

**解析器职责**：
- 读取结果文件
- 提取用例总数、通过/失败/跳过数、耗时
- 提取失败用例详情（用例名、文件路径、错误信息、堆栈）
- 提取覆盖率数据（若存在）
- 归一化为统一数据结构

### Step 5: 生成报告内容

**标准报告结构**（固定顺序）：

1. **报告头**
   - 项目名、生成时间、执行命令
   - 框架/版本、执行环境摘要

2. **结果摘要**
   - 用例总数、通过/失败/跳过数
   - 通过率、总耗时
   - 整体结论：✅ 通过 / ❌ 未通过

3. **失败用例分析**（有失败时必选）
   - 用例名、所属文件
   - 错误信息、堆栈关键行（截断至可读长度，最多 10 行）

4. **用例明细**
   - 按测试文件分组
   - 每个用例的状态与耗时
   - 超过 200 条时截断并注明

5. **覆盖率**（若可获取）
   - 语句/分支/函数/行覆盖率
   - 低于阈值的文件清单（默认阈值 80%）

6. **附录**
   - 原始结果文件路径
   - 生成工具版本

### Step 6: 格式化输出

**Markdown 格式**（默认）：

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

---

## 📈 覆盖率

| 类型 | 覆盖率 |
|------|--------|
| 语句 | {statements}% |
| 分支 | {branches}% |

---

## 📎 附录

- 原始结果文件: `{路径}`
- 生成工具版本: test-report-generator v1.0
```

**输出路径**：
- 默认：`reports/test-report-<YYYYMMDD-HHmmss>.md`
- 用户指定：按用户指定路径

### Step 7: 返回结果

向用户返回：
- 报告路径
- 结果摘要（通过率、失败数）
- 失败时附最关键的 1~3 条失败原因

```
✅ 测试报告已生成

📄 报告路径: reports/test-report-20260714-090000.md

📊 结果摘要:
- 用例总数: 42
- 通过: 40 ✅
- 失败: 2 ❌
- 通过率: 95.2%

❌ 关键失败用例:
1. [用户模块] 登录失败测试 - Expected status 200 but got 401
2. [订单模块] 支付超时测试 - Timeout exceeded 5000ms
```

## Common Rationalizations

**❌ "这个小项目不需要正式报告"**
→ 即使小项目，统一格式的报告也能减少沟通成本，建立质量意识。

**❌ "我可以直接看测试输出"**
→ CI 日志难以回溯，报告提供持久化、可分享的质量记录。

**❌ "格式太复杂，简简单单就好"**
→ 标准结构确保信息完整，自动化生成无需人工投入。

**❌ "覆盖率数据可能不准确"**
→ 报告如实呈现数据，并标注"未获取"以保持透明。

## Red Flags

🚨 **测试执行失败时生成空报告**
→ 必须返回明确错误诊断，不生成空报告冒充成功。

🚨 **报告中泄露敏感信息**
→ 错误堆栈需过滤环境变量、密钥类内容。

🚨 **跳过失败分析章节**
→ 有失败用例时，失败分析是必选章节，不得省略。

🚨 **覆盖率为空时用 0% 填充**
→ 未获取时应标注"未获取"，不应伪造数据。

## Verification

### 功能验收清单

- [ ] **AC1**: Jest/Vitest 项目执行"生成测试报告"，产出符合结构的 Markdown 报告，摘要数据与框架原始输出一致
- [ ] **AC2**: 存在失败用例时，报告包含用例名、文件路径、错误信息
- [ ] **AC3**: 提供 JUnit XML 文件走解析模式，不触发测试执行即可产出报告
- [ ] **AC4**: 结果文件损坏时，返回明确错误说明而非空报告
- [ ] **AC5**: 覆盖率数据存在时正确呈现，不存在时标注"未获取"

### 非功能验收清单

- [ ] **NFR1**: 1000 用例规模下，结果解析+报告生成 < 5s
- [ ] **NFR2**: 结果文件格式异常时降级输出，不崩溃
- [ ] **NFR3**: 报告中不泄露环境变量/密钥
- [ ] **NFR4**: 同一结果文件多次生成报告，内容一致（时间戳除外）

## Parser Implementation Guide

### Jest JSON Parser

```javascript
// Jest JSON Reporter 输出格式
{
  "success": boolean,
  "startTime": number,
  "numTotalTests": number,
  "numPassedTests": number,
  "numFailedTests": number,
  "numPendingTests": number,
  "testResults": [{
    "name": string,  // 测试文件路径
    "status": "passed" | "failed" | "pending",
    "startTime": number,
    "endTime": number,
    "message": string,
    "assertionResults": [{
      "ancestorTitles": string[],
      "fullName": string,
      "status": "passed" | "failed" | "pending",
      "title": string,
      "duration": number,
      "failureMessages": string[]
    }]
  }]
}
```

### JUnit XML Parser

```xml
<!-- JUnit XML 格式 -->
<testsuite name="suite" tests="42" failures="2" skipped="1" time="12.5">
  <testcase name="test_login" classname="AuthTest" time="0.5">
    <!-- passed -->
  </testcase>
  <testcase name="test_failure" classname="AuthTest" time="0.3">
    <failure message="Expected 200 but got 401" type="AssertionError">
      堆栈信息...
    </failure>
  </testcase>
  <testcase name="test_skipped" classname="AuthTest" time="0">
    <skipped message="Feature not implemented"/>
  </testcase>
</testsuite>
```

## Configuration Examples

### 解析模式

```
用户: 把 test-results/junit.xml 转成测试报告

Agent: 
1. 识别意图：解析模式
2. 定位文件：test-results/junit.xml
3. 选择解析器：JUnitParser
4. 解析结果 → 归一化数据
5. 生成 Markdown 报告
6. 返回报告路径与摘要
```

### 执行模式

```
用户: 跑一下测试并生成报告

Agent:
1. 识别意图：执行模式
2. 检测框架：发现 package.json → npm test
3. 执行测试：npm test -- --json --outputFile=test-results.json
4. 解析结果：JestParser
5. 生成报告
6. 返回结果
```

## Error Diagnostics

当测试执行或解析失败时，返回结构化错误：

```json
{
  "success": false,
  "error": {
    "code": "TEST_COMMAND_NOT_FOUND",
    "message": "未找到测试命令",
    "details": "请在配置中指定 test_command 或确保 package.json 包含 test 脚本"
  }
}
```

**错误代码表**：

| 代码 | 说明 |
|------|------|
| `TEST_COMMAND_NOT_FOUND` | 未找到测试命令 |
| `FRAMEWORK_NOT_SUPPORTED` | 当前框架不在支持列表 |
| `RESULT_FILE_INVALID` | 结果文件格式异常或损坏 |
| `EXECUTION_TIMEOUT` | 测试执行超时 |
| `PERMISSION_DENIED` | 无权限执行测试或写入报告 |

## Security Considerations

- ❌ 不在报告中输出环境变量值
- ❌ 不输出 API 密钥、数据库密码等凭据
- ✅ 过滤堆栈中的敏感路径（如 `/home/user/.env`）
- ✅ 错误信息截断处理，避免泄露完整上下文

## Performance Notes

- **目标**：1000 用例规模下解析+生成 < 5s
- **策略**：流式解析大文件，按需加载覆盖率数据
- **限制**：超过 200 条用例时明细表格截断，保持报告可读性