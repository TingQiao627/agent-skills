---
name: test-report-generation
description: 自动解析测试结果并生成结构化测试报告。支持 Jest/Vitest/pytest/JUnit XML，输出 Markdown/HTML/JSON 格式报告。
---

# 测试报告生成

## 概述

执行测试后自动收集、解析测试结果，生成结构化、可读性强的标准测试报告。支持主流测试框架，输出多种格式，包含摘要、明细、失败分析、覆盖率等完整章节。

## 何时使用

- 测试执行完成后需要生成标准化报告
- 将已有的 JUnit XML / JSON 结果文件转换为可读报告
- CI/CD 流程中需要统一的测试报告格式
- 需要向团队同步质量状态（通过率、覆盖率等指标）

**何时不用：** 非测试类质量报告（lint、安全扫描）、需要在线托管/Web 服务化展示。

## 触发意图

- "生成测试报告"
- "跑一下测试并出报告"
- "把这个 junit.xml 转成测试报告"
- "解析测试结果生成报告"

## 配置项

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `test_command` | 自动检测 | 测试执行命令（如 `npm test`、`pytest`） |
| `result_file` | 自动检测 | 解析模式下的结果文件路径 |
| `output_format` | markdown | 输出格式：markdown / html / json |
| `output_path` | reports/ | 报告输出目录 |
| `coverage` | auto | 覆盖率处理：auto / on / off |
| `fail_threshold` | 无 | 通过率低于该值时报告结论标记为不达标 |

## 工作模式

### 执行模式

Skill 触发测试运行并收集结果：

1. 检测项目测试框架与运行命令
2. 执行测试命令（支持后台执行长任务）
3. 收集测试结果文件（JSON/JUnit XML）
4. 解析结果并生成报告

### 解析模式

跳过执行，直接解析已有结果文件：

```
提供 JUnit XML 文件路径 → 解析 → 生成报告
```

适用于 CI 流程中复用已有测试结果，避免重复执行。

## 报告结构

生成的报告包含以下章节（顺序固定）：

1. **报告头**：项目名、生成时间、执行命令、框架/版本、执行环境摘要
2. **结果摘要**：用例总数、通过/失败/跳过数、通过率、总耗时；整体结论用 ✅ / ❌ 标识
3. **失败用例分析**（有失败时）：用例名、所属文件、错误信息、堆栈关键行
4. **用例明细**：按测试文件分组的用例列表与耗时（超过200条时截断）
5. **覆盖率**（若可获取）：语句/分支/函数/行覆盖率总表，低于阈值的文件清单
6. **附录**：原始结果文件路径、生成工具版本

## 支持的框架

### P0（首期支持）

- **JavaScript/TypeScript**：Jest、Vitest（JSON reporter）
- **Python**：pytest（JUnit XML / JSON report）
- **通用**：JUnit XML（跨语言兜底格式）

### P1（后续）

- Go test
- cargo test (Rust)
- .NET test

## 流程

### Step 1: 检测测试框架

按优先级检测：

1. 用户显式指定的命令
2. 项目配置文件（package.json scripts、pyproject.toml、Cargo.toml）
3. 框架特征文件（jest.config.*、vitest.config.*、pytest.ini）

### Step 2: 执行测试或读取结果

- **执行模式**：运行检测到的测试命令
- **解析模式**：读取指定的结果文件

失败时给出明确诊断，不生成空报告。

### Step 3: 解析结果

调用对应框架的解析器：

- Jest/Vitest：解析 JSON report
- pytest：解析 JUnit XML 或 JSON
- 通用：解析 JUnit XML

### Step 4: 生成报告

根据配置的输出格式生成报告：

- Markdown（默认）
- HTML（P1）
- JSON（结构化数据）

### Step 5: 返回结果

向用户返回：

- 报告路径
- 结果摘要（通过率、失败数）
- 失败时附最关键的 1~3 条失败原因

## 常见误区

### ❌ "测试命令执行失败就生成空报告"

错误。测试命令无法运行时，应返回明确诊断信息，说明环境问题、依赖缺失等，不得生成空报告冒充成功。

### ❌ "覆盖率数据缺失就跳过该章节"

错误。覆盖率数据不存在时，应标注"未获取"，其余章节正常生成。

### ❌ "报告内容越多越好"

错误。用例明细超过 200 条时应截断并注明，避免报告过于臃肿。失败堆栈应截断至可读长度（建议 10 行）。

## 警示信号

| 信号 | 说明 | 处理 |
|------|------|------|
| 结果文件损坏 | JSON/XML 解析失败 | 返回明确错误说明，列出缺失字段 |
| 测试执行超时 | 长时间无响应 | 建议后台执行并轮询，或提示用户检查测试用例 |
| 敏感信息泄露 | 报告包含环境变量、密钥 | 过滤敏感路径外的凭据信息 |

## 验证

- [ ] 在含 Jest 或 Vitest 的 TS 项目中执行，产出符合结构的 Markdown 报告
- [ ] 存在失败用例时，报告失败分析章节包含用例名、文件路径、错误信息
- [ ] 提供 JUnit XML 文件走解析模式，不触发测试执行即可产出报告
- [ ] 结果文件损坏时，返回明确错误说明而非空报告
- [ ] 覆盖率数据存在时正确呈现，不存在时标注"未获取"

## 示例

### 执行模式

```
用户: 生成测试报告

Agent:
1. 检测到 package.json 中的 Jest 配置
2. 执行 `npm test -- --json --outputFile=test-results.json`
3. 解析 test-results.json
4. 生成报告: reports/test-report-20260714-120000.md

📋 测试报告已生成: reports/test-report-20260714-120000.md
✅ 通过率: 95% (57/60)
❌ 失败用例: 3
  - UserService › should create user
  - AuthService › should validate token
  - OrderService › should calculate total
```

### 解析模式

```
用户: 把 test-results/junit.xml 转成测试报告

Agent:
1. 读取 test-results/junit.xml
2. 解析 JUnit XML 格式
3. 生成报告: reports/test-report-20260714-120500.md

📋 测试报告已生成
```

## 参考

- [Jest JSON Output](https://jestjs.io/docs/cli#--json)
- [Vitest JSON Reporter](https://vitest.dev/config/#reporters)
- [JUnit XML Format](https://llg.cubic.org/docs/junit/)
- [pytest JUnit XML](https://docs.pytest.org/en/stable/how-to/output.html#creating-junitxml-format-reports)