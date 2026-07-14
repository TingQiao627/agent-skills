# test-report-generation Skill 实施计划

> 创建时间：2026-07-14
> 里程碑：M1（P0）
> 状态：待执行

---

## 1. 目标

实现 test-report-generation Skill 的 M1 里程碑（P0），完成以下核心能力：
- Jest / Vitest / JUnit XML 三种测试框架的结果解析
- 标准化 Markdown 报告生成
- 执行模式与解析模式双支持

---

## 2. 验收标准

| 编号 | 验收要点 |
|------|----------|
| AC1 | 在 Jest/Vitest 项目中执行 Skill 能产出符合结构的 Markdown 报告 |
| AC2 | 存在失败用例时报告包含用例名、文件路径、错误信息 |
| AC3 | 提供 JUnit XML 文件走解析模式能产出报告 |
| AC4 | 结果文件损坏时返回明确错误说明而非空报告 |
| AC5 | 覆盖率数据存在时正确呈现，不存在时标注"未获取" |

---

## 3. 影响范围

```
skills/test-report-generation/
├── SKILL.md                      # Skill 主文档
├── scripts/
│   ├── parse-jest.sh             # Jest JSON 解析器
│   ├── parse-vitest.sh           # Vitest JSON 解析器
│   ├── parse-junit.sh            # JUnit XML 解析器
│   └── generate-report.sh        # 报告生成器
└── supporting/
    └── schema.md                 # 统一数据结构定义
```

---

## 4. 风险与缓解

| 风险 | 缓解措施 |
|------|----------|
| 各框架 JSON 输出差异大 | 插件式架构 + 统一数据结构抽象层 |
| 测试执行长任务阻塞 | 支持后台执行与轮询状态 |

---

## 5. 回滚策略

删除 `skills/test-report-generation/` 目录即可完整回滚，无仓库其他文件副作用。

---

## 6. 验证清单

- [ ] 执行 `parse-jest.sh` 解析 Jest JSON 样本验证输出结构
- [ ] 执行 `parse-vitest.sh` 解析 Vitest JSON 样本验证输出结构
- [ ] 执行 `parse-junit.sh` 解析 JUnit XML 样本验证输出结构
- [ ] 执行 `generate-report.sh` 生成完整报告并检查六大章节齐全
- [ ] 输入损坏 JSON/XML 文件验证错误处理返回明确诊断信息

---

## 7. 实施步骤

### Step 1: 创建 Skill 目录结构与 SKILL.md 主文档
- 创建 `skills/test-report-generation/` 目录
- 编写 SKILL.md，包含：
  - Skill 元数据（name, description）
  - When to Use 触发条件
  - Process 执行流程（测试执行 → 结果收集 → 报告生成）
  - Configuration 配置项说明
  - Verification 验证方法

### Step 2: 定义统一数据结构 schema
- 文件：`supporting/schema.md`
- 核心类型：
  - `TestResult`: 总体结果（total, passed, failed, skipped, duration）
  - `TestCase`: 单个用例（name, file, status, duration, error）
  - `Coverage`: 覆盖率（lines, branches, functions）
- 输出格式：JSON（内部流转用）

### Step 3: 实现 Jest JSON 解析器
- 文件：`scripts/parse-jest.sh`
- 输入：Jest `--json` 输出
- 输出：统一结构 JSON
- 关键处理：
  - 解析 `success`, `numPassedTests`, `numFailedTests`, `testResults`
  - 提取失败用例的 `message`, `stack`
  - 转换为统一 `TestCase` 数组

### Step 4: 实现 Vitest JSON 解析器
- 文件：`scripts/parse-vitest.sh`
- 输入：Vitest `--reporter=json` 输出
- 输出：统一结构 JSON
- 关键处理：
  - 解析 `testResults` 数组
  - 处理 Vitest 特有的嵌套结构

### Step 5: 实现 JUnit XML 解析器
- 文件：`scripts/parse-junit.sh`
- 输入：标准 JUnit XML
- 输出：统一结构 JSON
- 关键处理：
  - 解析 `<testsuite>` 和 `<testcase>` 元素
  - 提取 `<failure>` 和 `<error>` 内容
  - 处理 `time` 属性转换为秒数

### Step 6: 实现报告生成脚本
- 文件：`scripts/generate-report.sh`
- 输入：统一结构 JSON
- 输出：Markdown 报告
- 报告结构：
  1. 报告头（项目名、时间、命令、框架、环境）
  2. 结果摘要（总数、通过/失败/跳过、通过率、耗时）
  3. 失败用例分析（用例名、文件、错误、堆栈）
  4. 用例明细（按文件分组、折叠超过200条）
  5. 覆盖率（若存在）
  6. 附录（原始文件路径、工具版本）

### Step 7: 执行验收测试
- 准备测试数据：
  - Jest JSON 样本（含成功和失败场景）
  - Vitest JSON 样本
  - JUnit XML 样本
  - 损坏文件样本
- 执行 AC1-AC5 验证
- 修复发现的问题

---

## 8. 依赖与约束

- 依赖：`jq`（JSON 处理）、`xmllint` 或 `yq`（XML 解析）
- 约束：不生成代码修复建议，不托管报告，不做趋势分析