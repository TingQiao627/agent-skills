# Spec: test-report-generation

## Overview
定义 test-report-generator Skill 的用户可见需求与场景。

---

## FR1：测试执行与结果收集

### FR1.1 自动识别测试框架与运行命令
优先级识别顺序：
1. 用户显式指定的命令
2. `package.json` scripts（test）、`pyproject.toml`、`Cargo.toml` 等项目配置
3. 框架特征文件推断（如 `jest.config.*`、`vitest.config.*`、`pytest.ini`）

**Scenario: 用户指定命令**
- Given 用户提供了 `test_command: "npx vitest run --reporter=json"`
- When Skill 开始执行
- Then 使用用户指定的命令执行测试，不进行自动检测

**Scenario: 自动检测 Jest 项目**
- Given 项目根目录存在 `jest.config.ts` 和 `package.json`（含 `"test": "jest"`）
- And 用户未指定 `test_command`
- When Skill 开始执行
- Then 自动推断使用 `npx jest --json` 执行测试

**Scenario: 自动检测 Vitest 项目**
- Given 项目根目录存在 `vitest.config.ts`
- And 用户未指定 `test_command`
- When Skill 开始执行
- Then 自动推断使用 `npx vitest run --reporter=json` 执行测试

**Scenario: 自动检测 pytest 项目**
- Given 项目根目录存在 `pytest.ini` 或 `pyproject.toml`（含 `[tool.pytest]`）
- And 用户未指定 `test_command`
- When Skill 开始执行
- Then 自动推断使用 `pytest --junitxml=...` 执行测试

### FR1.2 首期支持框架（P0）
- JavaScript/TypeScript：Jest（JSON reporter）、Vitest（JSON reporter）
- Python：pytest（JUnit XML / JSON report）
- 通用：JUnit XML（跨语言兜底格式）

**Scenario: 解析 Jest JSON 结果**
- Given 存在 `jest-results.json`（Jest JSON reporter 输出）
- When Skill 解析该文件
- Then 正确提取用例总数、通过/失败/跳过数、总耗时、用例明细

**Scenario: 解析 Vitest JSON 结果**
- Given 存在 `vitest-results.json`（Vitest JSON reporter 输出）
- When Skill 解析该文件
- Then 正确提取用例总数、通过/失败/跳过数、总耗时、用例明细

**Scenario: 解析 JUnit XML 结果**
- Given 存在 `junit.xml`（标准 JUnit XML 格式）
- When Skill 解析该文件
- Then 正确提取 testsuite 层级的用例统计与 testcase 明细

### FR1.3 两种工作模式

**Scenario: 执行模式**
- Given 用户触发"跑测试并生成报告"
- And 用户未指定 `result_file`
- When Skill 执行
- Then 先执行测试命令，收集结果文件，再解析生成报告

**Scenario: 解析模式（跳过执行）**
- Given 用户指定 `result_file: "test-results/junit.xml"`
- When Skill 执行
- Then 跳过测试执行，直接解析该文件并生成报告

### FR1.4 测试执行失败诊断
**Scenario: 测试命令无法运行**
- Given 用户指定的 `test_command` 在环境中不存在
- When Skill 尝试执行
- Then 输出明确诊断信息（如 "命令 'xxx' 未找到，请检查环境"），不生成空报告

**Scenario: 测试框架未识别**
- Given 项目根目录无任何已知框架特征文件
- And 用户未指定 `test_command`
- When Skill 执行
- Then 输出诊断信息（如 "未检测到已知测试框架，请显式指定 test_command"），不生成空报告

---

## FR2：报告内容结构

报告必须包含以下章节，顺序固定：
1. 报告头
2. 结果摘要
3. 失败用例分析（有失败时必选）
4. 用例明细
5. 覆盖率（若可获取）
6. 附录

**Scenario: 全量成功生成完整报告**
- Given 测试全部通过，覆盖率为 80%
- When 生成报告
- Then 报告包含报告头、摘要（✅ 通过）、用例明细、覆盖率、附录
- And 失败用例分析章节不出现

**Scenario: 存在失败用例时生成报告**
- Given 测试有 3 个失败用例
- When 生成报告
- Then 摘要标记 ❌ 不通过
- And 失败用例分析章节包含每条失败用例的：用例名、所属文件、错误信息、堆栈关键行

**Scenario: 覆盖率不可获取**
- Given 项目未配置覆盖率工具
- When 生成报告
- Then 覆盖率章节标注"未获取"
- And 其余章节正常生成

**Scenario: 用例数超过 200 条截断**
- Given 测试共 350 条用例
- When 生成报告
- Then 用例明细章节展示前 200 条并注明 "共 350 条用例，已截断展示前 200 条"

---

## FR3：输出格式与落盘

### FR3.1 输出格式
- 默认：Markdown（.md）
- P1：HTML（.html）、JSON（.json）伴随产物

**Scenario: 默认生成 Markdown**
- Given 用户未指定 `output_format`
- When 生成报告
- Then 输出 `.md` 文件

### FR3.2 输出路径
- 默认：`reports/test-report-<YYYYMMDD-HHmmss>.md`
- 用户可通过 `output_path` 指定目录

**Scenario: 默认路径**
- Given 用户未指定 `output_path`
- When 生成报告
- Then 报告落盘到 `reports/test-report-<YYYYMMDD-HHmmss>.md`

**Scenario: 用户指定路径**
- Given 用户指定 `output_path: "my-reports/"`
- When 生成报告
- Then 报告落盘到 `my-reports/test-report-<YYYYMMDD-HHmmss>.md`

### FR3.3 生成后返回信息
**Scenario: 生成成功反馈**
- Given 报告生成成功
- When Skill 完成
- Then 返回：报告路径 + 结果摘要（通过率、失败数）
- And 存在失败时附最关键的 1~3 条失败原因

---

## FR4：Skill 交互约定

### FR4.1 触发意图
- "生成测试报告"
- "跑一下测试并出报告"
- "把这个 junit.xml 转成测试报告"

### FR4.2 可配置项

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `test_command` | 自动检测 | 测试执行命令 |
| `result_file` | 自动检测 | 解析模式下的结果文件路径 |
| `output_format` | `markdown` | `markdown` / `html` / `json` |
| `output_path` | `reports/` | 报告输出目录 |
| `coverage` | `auto` | `auto` / `on` / `off` |
| `fail_threshold` | 无 | 通过率低于该值时报告结论标记为不达标 |

**Scenario: 用户覆盖默认配置**
- Given 用户指定 `output_format: "html"`, `fail_threshold: "80%"`
- When 生成报告
- Then 输出 HTML 格式报告
- And 结果摘要中，若通过率低于 80% 则标记为不达标

---

## NFR：非功能需求

### NFR1 性能
- 结果解析与报告生成（不含测试执行）应在 5 秒内完成（1000 用例规模）

### NFR2 健壮性
- 结果文件格式异常、字段缺失时降级输出（缺失项标注"未获取"），不得崩溃或静默丢数据

**Scenario: 结果文件字段缺失**
- Given 结果文件中某用例缺少 `duration` 字段
- When 解析该文件
- Then 该用例耗时标注为"未获取"
- And 其他用例正常展示

### NFR3 安全
- 报告中不得泄露环境变量、密钥类内容
- 错误堆栈须过滤敏感路径外的凭据信息

### NFR4 幂等性
- 同一结果文件多次生成报告，内容一致（时间戳字段除外）

### NFR5 可维护性
- 框架解析器采用插件式结构，新增框架支持不影响既有解析器

---

## 验收标准

- **AC1**：在含 Jest 或 Vitest 的 TS 项目中执行"生成测试报告"，产出符合 FR2 结构的 Markdown 报告，摘要数据与框架原始输出一致
- **AC2**：存在失败用例时，报告失败分析章节包含用例名、文件路径、错误信息
- **AC3**：提供 JUnit XML 文件走解析模式，不触发测试执行即可产出报告
- **AC4**：结果文件损坏时，Skill 返回明确错误说明而非空报告
- **AC5**：覆盖率数据存在时正确呈现，不存在时标注"未获取"且其余章节正常