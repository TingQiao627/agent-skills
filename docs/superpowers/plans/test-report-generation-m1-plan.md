# 测试报告生成 Skill 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现 test-report-generation Skill，支持 Jest/Vitest/pytest/JUnit XML 测试结果解析，生成标准化 Markdown 报告。

**Architecture:** 采用插件式解析器架构，每个测试框架一个独立解析脚本，输出统一 JSON 数据结构，由报告生成器统一处理。核心流程：模式判断 → 框架检测 → 结果解析 → 报告生成。

**Tech Stack:** 
- Skill 框架: SKILL.md + shell scripts
- 解析器: bash + jq (JSON), xmllint (XML)
- 报告模板: Markdown + Jinja2-style 变量替换
- 首期目标: TypeScript/Node.js (Jest/Vitest)

---

## Global Constraints

从规格文档提取的全局约束：

- 报告必须包含六大章节：报告头、结果摘要、失败分析、用例明细、覆盖率、附录
- 默认输出路径：`reports/test-report-<YYYYMMDD-HHmmss>.md`
- 支持执行/解析双模式
- 首期仅中文模板
- 不做测试生成、报告托管、趋势对比、非测试质量报告
- 解析器输出统一 JSON 结构
- 错误处理：字段缺失标注"未获取"，不崩溃

---

## Task 1: 创建 Skill 基础结构与 SKILL.md

**Files:**
- Create: `skills/test-report-generation/SKILL.md`

**Interfaces:**
- Consumes: N/A
- Produces: Skill 元数据（name、description、触发意图）

**Steps:**

- [ ] 创建目录结构 `skills/test-report-generation/`
- [ ] 编写 SKILL.md frontmatter：
  ```yaml
  ---
  name: test-report-generation
  description: 在测试执行后自动解析测试结果并生成结构化标准测试报告。使用场景：生成测试报告、跑测试并出报告、将 JUnit XML/JSON 转换为报告。
  ---
  ```
- [ ] 编写 Overview 章节：说明 Skill 的核心价值（一条指令完成执行→收集→报告）
- [ ] 编写 When to Use 章节：列出触发意图示例
  - "生成测试报告"
  - "跑一下测试并出报告"
  - "把这个 junit.xml 转成测试报告"
  - "解析测试结果生成报告"
- [ ] 编写 Process 章节：详细流程步骤
  1. 判断模式（执行 vs 解析）
  2. 检测测试框架（Jest/Vitest/pytest/JUnit XML）
  3. 执行测试命令（执行模式）或读取结果文件（解析模式）
  4. 调用对应解析器生成统一 JSON
  5. 调用报告生成器生成 Markdown
  6. 返回报告路径 + 摘要
- [ ] 编写 Common Rationalizations 章节：列出常见错误认知
  - "这只是简单的文件转换" → 实际需要框架特定的解析逻辑
  - "我可以手动运行测试" → Skill 价值在于自动化全流程
- [ ] 编写 Red Flags 章节：列出异常场景
  - 测试命令无法运行 → 返回诊断信息，不生成报告
  - 结果文件损坏 → 返回明确错误说明
  - 覆盖率数据不存在 → 正常生成报告，标注"未获取"
- [ ] 编写 Verification 章节：验收标准
  - AC1: Jest/Vitest 项目执行"生成测试报告"，产出符合结构的 Markdown
  - AC2: 失败用例报告包含用例名、文件路径、错误信息
  - AC3: JUnit XML 解析模式不触发测试执行
  - AC4: 结果文件损坏返回明确错误
  - AC5: 覆盖率数据正确呈现或标注"未获取"

**Verification:**
- [ ] 文件路径正确：`skills/test-report-generation/SKILL.md`
- [ ] 包含完整的 YAML frontmatter
- [ ] 所有必选章节已填写
- [ ] 触发意图示例与规格文档一致

---

## Task 2: 创建数据结构定义文档

**Files:**
- Create: `skills/test-report-generation/supporting/schema.md`

**Interfaces:**
- Consumes: 规格文档 8.2 节统一数据结构
- Produces: 解析器输出 JSON Schema 定义

**Steps:**

- [ ] 创建目录 `skills/test-report-generation/supporting/`
- [ ] 编写数据结构文档，包含以下字段定义：
  - `summary`: total, passed, failed, skipped, duration
  - `failures`: name, file, error, stack (数组)
  - `cases`: file, name, status, duration
  - `coverage`: statements, branches, functions, lines, low_coverage_files (数组)
  - `metadata`: framework, version, command, timestamp
- [ ] 为每个字段添加类型说明和示例值
- [ ] 添加必选/可选标记（summary 必选，coverage 可选）

**Verification:**
- [ ] JSON 结构与规格文档 8.2 节一致
- [ ] 所有字段均有类型说明
- [ ] 包含示例 JSON

---

## Task 3: 实现 Jest JSON 解析器

**Files:**
- Create: `skills/test-report-generation/scripts/parse-jest.sh`

**Interfaces:**
- Consumes: Jest JSON reporter 输出文件
- Produces: 统一 JSON 结构（Task 2 定义）

**Steps:**

- [ ] 创建脚本骨架，接收参数：`--input <jest-json-file>`
- [ ] 使用 `jq` 解析 Jest JSON 结构：
  - 提取 `numTotalTests`, `numPassedTests`, `numFailedTests`, `numPendingTests`
  - 计算 duration (从 `startTime` 和 `endTime` 或累加各用例耗时)
- [ ] 解析失败用例：
  - 遍历 `testResults[].assertionResults`，筛选 `status == "failed"`
  - 提取 `ancestorTitles` + `title` 组合为用例名
  - 提取 `failureMessages[0]` 为错误信息
  - 提取堆栈前 5 行（过滤敏感路径）
- [ ] 解析所有用例明细：
  - 提取每个用例的 file, name, status, duration
- [ ] 解析覆盖率（若存在）：
  - 从 `coverageMap` 提取语句/分支/函数/行覆盖率
  - 识别低于阈值的文件
- [ ] 构建统一 JSON 结构并输出到 stdout
- [ ] 错误处理：
  - 输入文件不存在 → 返回错误代码 1
  - JSON 格式错误 → 返回错误代码 2
  - 字段缺失 → 标注"未获取"

**Verification:**
- [ ] 使用示例 Jest JSON 测试（从 `npm test -- --json` 生成）
- [ ] 输出 JSON 符合 Task 2 定义的 Schema
- [ ] 失败用例正确提取
- [ ] 堆栈过滤敏感信息

---

## Task 4: 实现 Vitest JSON 解析器

**Files:**
- Create: `skills/test-report-generation/scripts/parse-vitest.sh`

**Interfaces:**
- Consumes: Vitest JSON reporter 输出
- Produces: 统一 JSON 结构（Task 2 定义）

**Steps:**

- [ ] 创建脚本骨架，接收参数：`--input <vitest-json-file>`
- [ ] 使用 `jq` 解析 Vitest JSON：
  - 提取 `testResults` 中的总数/通过/失败/跳过数
  - 计算 duration（累加各用例耗时）
- [ ] 解析失败用例：
  - 遍历 `testResults`，筛选失败用例
  - 提取用例名、文件路径、错误信息
  - 截断堆栈至前 5 行
- [ ] 解析所有用例明细
- [ ] 解析覆盖率（Vitest 内置 coverage）
- [ ] 构建统一 JSON 输出
- [ ] 错误处理：同 Task 3

**Verification:**
- [ ] 使用示例 Vitest JSON 测试
- [ ] 输出 JSON 符合 Schema
- [ ] 覆盖率正确提取

---

## Task 5: 实现 JUnit XML 解析器

**Files:**
- Create: `skills/test-report-generation/scripts/parse-junit.sh`

**Interfaces:**
- Consumes: JUnit XML 文件（pytest/Jest/Maven Surefire）
- Produces: 统一 JSON 结构（Task 2 定义）

**Steps:**

- [ ] 创建脚本骨架，接收参数：`--input <junit-xml-file>`
- [ ] 使用 `xmllint` 或 `xmlstarlet` 解析 XML：
  - 提取 `<testsuite>` 或 `<testsuites>` 的 tests, failures, skipped 属性
  - 计算 passed = tests - failures - skipped
- [ ] 解析失败用例：
  - 查找 `<testcase>` 中包含 `<failure>` 或 `<error>` 的节点
  - 提取 `name` 属性、`classname` 属性（作为文件路径）
  - 提取 `<failure>` 内容为错误信息
- [ ] 解析所有用例：
  - 遍历所有 `<testcase>` 节点
  - 提取 name, classname (file), time
  - 判断状态：有 `<failure>` → failed, 有 `<skipped>` → skipped, 其他 → passed
- [ ] 覆盖率：JUnit XML 通常不含覆盖率，标注"未获取"
- [ ] 构建统一 JSON 输出
- [ ] 错误处理：XML 解析失败返回明确错误

**Verification:**
- [ ] 使用示例 pytest JUnit XML 测试
- [ ] 输出 JSON 符合 Schema
- [ ] 失败用例正确提取

---

## Task 6: 实现报告生成脚本

**Files:**
- Create: `skills/test-report-generation/scripts/generate-report.sh`

**Interfaces:**
- Consumes: 统一 JSON 数据（Task 2 定义）
- Produces: Markdown 报告文件

**Steps:**

- [ ] 创建脚本骨架，接收参数：`--input <unified-json> --output <report-path> --format markdown`
- [ ] 实现报告模板引擎（使用 envsubst 或简单的 sed 替换）：
  ```bash
  # 读取 JSON 并提取字段
  total=$(jq -r '.summary.total' "$input")
  passed=$(jq -r '.summary.passed' "$input")
  failed=$(jq -r '.summary.failed' "$input")
  skipped=$(jq -r '.summary.skipped' "$input")
  duration=$(jq -r '.summary.duration' "$input")
  pass_rate=$(echo "scale=1; $passed * 100 / $total" | bc)
  ```
- [ ] 生成报告头章节：
  ```markdown
  # 测试报告
  
  - **项目名称**: [从 metadata 或当前目录名]
  - **生成时间**: [当前时间 YYYY-MM-DD HH:mm:ss]
  - **执行命令**: [从 metadata.command]
  - **测试框架**: [从 metadata.framework + version]
  - **执行环境**: [Node.js 版本 + OS]
  ```
- [ ] 生成结果摘要章节：
  ```markdown
  ## 📊 结果摘要
  
  | 指标 | 数值 |
  |------|------|
  | 用例总数 | $total |
  | ✅ 通过 | $passed |
  | ❌ 失败 | $failed |
  | ⏭️ 跳过 | $skipped |
  | 通过率 | ${pass_rate}% |
  | 总耗时 | ${duration}s |
  
  **整体结论**: ✅ 测试通过 / ❌ 存在失败用例
  ```
- [ ] 生成失败用例分析章节（若 failures 数组非空）：
  - 遍历 `failures` 数组
  - 为每个失败用例生成：
    ```markdown
    ### 用例 N: [name]
    - **所属文件**: `[file]`
    - **错误信息**: 
      ```
      [error]
      ```
    - **堆栈关键行**:
      ```
      [stack 前 5 行]
      ```
    ```
- [ ] 生成用例明细章节：
  - 按文件分组用例
  - 为每个文件生成表格：
    ```markdown
    ### [文件路径]
    | 用例名 | 状态 | 耗时 |
    |--------|------|------|
    | test_a | ✅ | 120ms |
    ```
  - 若用例数 > 200，截断并添加提示
- [ ] 生成覆盖率章节：
  - 若 coverage 存在：
    ```markdown
    ## 📈 覆盖率
    
    | 类型 | 覆盖率 | 状态 |
    |------|--------|------|
    | 语句覆盖率 | ${coverage.statements}% | ✅/⚠️ |
    ...
    
    ### ⚠️ 低覆盖率文件
    - `[file]`: ${coverage}%
    ```
  - 若不存在：
    ```markdown
    ## 📈 覆盖率
    
    > ⚠️ 未获取覆盖率数据
    ```
- [ ] 生成附录章节：
  ```markdown
  ## 📎 附录
  
  - **原始结果文件**: `[路径]`
  - **生成工具**: test-report-generation v1.0
  - **报告格式**: Markdown
  ```
- [ ] 写入文件到指定路径（默认 `reports/test-report-<timestamp>.md`）
- [ ] 输出摘要信息到 stdout：
  ```
  ✅ 报告生成成功
  - 路径: [report-path]
  - 通过率: [XX.X%]
  - 失败数: [N]
  [若有失败] 前 3 条失败原因:
  1. [用例名]: [错误信息前 100 字符]
  ```

**Verification:**
- [ ] 使用 Task 3-5 的输出 JSON 测试
- [ ] 报告包含所有六大章节
- [ ] Markdown 格式正确渲染
- [ ] 截断逻辑正确（> 200 用例）
- [ ] 时间戳格式正确

---

## Task 7: 实现框架自动检测逻辑

**Files:**
- Modify: `skills/test-report-generation/SKILL.md` (在 Process 章节中补充)

**Interfaces:**
- Consumes: 项目文件系统
- Produces: 框架类型、测试命令、结果文件路径

**Steps:**

- [ ] 实现检测逻辑（优先级从高到低）：
  1. 用户显式指定（通过 Skill 参数）
  2. 检查项目配置文件：
     - `package.json` → scripts.test 字段 → 判断包含 `jest` / `vitest`
     - `pyproject.toml` → 检查 pytest 配置
     - `pytest.ini` → pytest
  3. 检查框架特征文件：
     - `jest.config.*` 存在 → Jest
     - `vitest.config.*` 存在 → Vitest
  4. 扫描结果文件（解析模式）：
     - `test-results.json` → Jest/Vitest JSON
     - `junit.xml` / `test-results.xml` → JUnit XML
- [ ] 在 SKILL.md 的 Process 章节补充检测流程说明
- [ ] 添加检测失败的处理：提示用户手动指定框架

**Verification:**
- [ ] 在 Jest 项目中正确检测为 Jest
- [ ] 在 Vitest 项目中正确检测为 Vitest
- [ ] 无配置文件时提示用户指定

---

## Task 8: 实现执行/解析双模式

**Files:**
- Modify: `skills/test-report-generation/SKILL.md`

**Interfaces:**
- Consumes: 用户意图（是否有结果文件路径）
- Produces: 测试执行或直接解析

**Steps:**

- [ ] 实现模式判断逻辑：
  - 若用户指定结果文件路径（如 `--result-file junit.xml`）→ 解析模式
  - 否则 → 执行模式
- [ ] 实现执行模式：
  - 调用框架检测逻辑（Task 7）
  - 执行测试命令，使用框架特定的 reporter 参数：
    - Jest: `npm test -- --json --outputFile test-results.json`
    - Vitest: `vitest --reporter=json --outputFile test-results.json`
    - pytest: `pytest --junit-xml=junit.xml`
  - 收集测试输出文件
  - 调用对应解析器（Task 3-5）
- [ ] 实现解析模式：
  - 检测结果文件格式（JSON vs XML）
  - 调用对应解析器
- [ ] 错误处理：
  - 执行失败（非用例失败）→ 返回诊断信息，不生成报告
  - 结果文件损坏 → 返回解析错误

**Verification:**
- [ ] 执行模式：在 Jest 项目中成功执行并生成报告
- [ ] 解析模式：提供 JUnit XML 不触发测试执行
- [ ] 执行失败场景正确处理

---

## Task 9: 创建示例测试数据和文档

**Files:**
- Create: `skills/test-report-generation/examples/jest-results.json`
- Create: `skills/test-report-generation/examples/vitest-results.json`
- Create: `skills/test-report-generation/examples/junit-results.xml`
- Create: `skills/test-report-generation/examples/sample-report.md`

**Interfaces:**
- Consumes: N/A
- Produces: 示例数据和预期输出

**Steps:**

- [ ] 创建 Jest 示例 JSON：
  - 包含 5 个用例（3 通过、1 失败、1 跳过）
  - 失败用例含错误信息和堆栈
- [ ] 创建 Vitest 示例 JSON：
  - 包含 3 个用例（2 通过、1 失败）
- [ ] 创建 JUnit XML 示例：
  - pytest 风格，包含失败用例
- [ ] 使用示例数据运行生成脚本，创建示例报告
- [ ] 在 SKILL.md 中添加 Examples 章节引用示例文件

**Verification:**
- [ ] 示例 JSON/XML 格式正确
- [ ] 示例报告包含所有章节
- [ ] 示例可复现（脚本可运行）

---

## Task 10: 集成测试与验收

**Files:**
- Modify: `skills/test-report-generation/SKILL.md` (补充 Verification 章节)

**Interfaces:**
- Consumes: 所有前面任务的交付物
- Produces: 验收确认

**Steps:**

- [ ] 验收标准 AC1：Jest/Vitest 项目执行"生成测试报告"
  - 准备真实 Jest/Vitest 项目
  - 调用 Skill
  - 验证报告结构符合规格
- [ ] 验收标准 AC2：失败用例分析
  - 使用包含失败用例的测试数据
  - 验证报告包含用例名、文件路径、错误信息
- [ ] 验收标准 AC3：解析模式
  - 提供示例 JUnit XML
  - 验证不触发测试执行
  - 验证报告正确生成
- [ ] 验收标准 AC4：结果文件损坏
  - 使用损坏的 JSON/XML
  - 验证返回明确错误说明（非空报告）
- [ ] 验收标准 AC5：覆盖率数据
  - 测试有覆盖率数据的场景
  - 测试无覆盖率数据的场景
  - 验证标注"未获取"
- [ ] 更新 SKILL.md 的 Verification 章节记录测试结果

**Verification:**
- [ ] 所有 AC 验收通过
- [ ] 无 placeholder 或 TBD
- [ ] 文档完整（SKILL.md、schema.md、示例）

---

## Summary

**Completed files:**
- `skills/test-report-generation/SKILL.md`
- `skills/test-report-generation/scripts/parse-jest.sh`
- `skills/test-report-generation/scripts/parse-vitest.sh`
- `skills/test-report-generation/scripts/parse-junit.sh`
- `skills/test-report-generation/scripts/generate-report.sh`
- `skills/test-report-generation/supporting/schema.md`
- `skills/test-report-generation/examples/*` (4 files)

**Total tasks:** 10
**Estimated effort:** 2-3 hours with subagent-driven execution

**Key risks:**
- R1: 解析器需要处理各框架的输出差异 → 通过统一 JSON 结构和错误处理缓解
- R2: 测试执行耗时不可控 → 执行模式交给 Agent 运行时处理

**Next steps after plan approval:**
1. 执行 Task 1-2 创建基础结构
2. 执行 Task 3-6 实现核心解析器和生成器
3. 执行 Task 7-8 完善检测和双模式
4. 执行 Task 9-10 测试验收