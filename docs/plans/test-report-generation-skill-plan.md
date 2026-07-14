# 自动生成测试报告 Skill 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 创建一个 Agent Skill，能够在测试执行后自动解析测试结果并生成结构化、可读性强的标准测试报告（Markdown 格式）。

**Architecture:** 采用插件式解析器架构，核心流程为：测试执行/结果发现 → 框架检测 → 结果解析 → 报告生成。解析器按框架类型分文件实现，报告生成器支持多格式输出。

**Tech Stack:** TypeScript, Node.js, Zod（schema 校验）, 无外部依赖的纯解析逻辑

---

## Global Constraints

- 支持框架（P0）：Jest JSON, Vitest JSON, pytest JUnit XML, 通用 JUnit XML
- 默认输出格式：Markdown
- 默认输出路径：`reports/test-report-<YYYYMMDD-HHmmss>.md`
- 报告必须包含：报告头、结果摘要、失败用例分析、用例明细、覆盖率（可选）、附录
- 不使用 grep/glob 工具
- 遵循 skill-anatomy.md 的文件结构规范

---

## File Structure

```
skills/test-report-generation/
├── SKILL.md                    # Skill 主文件
├── scripts/
│   ├── index.ts                # 入口：CLI + 主流程编排
│   ├── executor.ts             # 测试执行逻辑
│   ├── detector.ts             # 框架检测逻辑
│   ├── parsers/
│   │   ├── base.ts             # 解析器基类 + 共享类型
│   │   ├── jest.ts             # Jest JSON 解析器
│   │   ├── vitest.ts           # Vitest JSON 解析器
│   │   ├── pytest.ts           # pytest JUnit XML 解析器
│   │   └── junit-xml.ts        # 通用 JUnit XML 解析器
│   ├── reporter/
│   │   ├── markdown.ts         # Markdown 报告生成器
│   │   └── templates.ts        # 报告模板常量
│   └── utils/
│       ├── file.ts             # 文件读写工具
│       └── format.ts           # 格式化工具（时间、百分比等）
└── package.json                # Node.js 项目配置
```

---

## Task 1: 项目骨架与类型定义

**Files:**
- Create: `skills/test-report-generation/SKILL.md`
- Create: `skills/test-report-generation/package.json`
- Create: `skills/test-report-generation/tsconfig.json`
- Create: `skills/test-report-generation/scripts/index.ts`
- Create: `skills/test-report-generation/scripts/parsers/base.ts`

**Interfaces:**

```typescript
// scripts/parsers/base.ts

/** 测试用例结果 */
export interface TestCaseResult {
  name: string;           // 用例名称
  file: string;           // 所属文件路径
  status: 'passed' | 'failed' | 'skipped' | 'pending';
  duration?: number;      // 执行耗时（毫秒）
  error?: {
    message: string;      // 错误信息
    stack?: string;       // 堆栈摘要（截断）
  };
}

/** 测试文件分组 */
export interface TestFileGroup {
  file: string;
  cases: TestCaseResult[];
  passed: number;
  failed: number;
  skipped: number;
  duration: number;
}

/** 覆盖率数据 */
export interface CoverageData {
  lines?: { covered: number; total: number };
  statements?: { covered: number; total: number };
  branches?: { covered: number; total: number };
  functions?: { covered: number; total: number };
}

/** 测试结果摘要 */
export interface TestResultSummary {
  project: string;
  timestamp: string;
  framework: string;
  frameworkVersion?: string;
  command: string;
  total: number;
  passed: number;
  failed: number;
  skipped: number;
  duration: number;       // 总耗时（毫秒）
  success: boolean;       // 通过率是否达标
  files: TestFileGroup[];
  coverage?: CoverageData;
  resultFiles: string[];  // 原始结果文件路径
}

/** 解析器接口 */
export interface TestResultParser {
  name: string;
  parse(content: string, filePath: string): Promise<TestResultSummary>;
}

/** 配置项 */
export interface SkillConfig {
  testCommand?: string;
  resultFile?: string;
  outputFormat: 'markdown' | 'html' | 'json';
  outputPath: string;
  coverage: 'auto' | 'on' | 'off';
  failThreshold?: number;
}
```

**Steps:**

- [ ] 创建 `skills/test-report-generation/` 目录
- [ ] 创建 `SKILL.md`，填写 YAML frontmatter：
  ```yaml
  ---
  name: test-report-generation
  description: 自动生成测试报告。在测试执行后解析结果并生成结构化的 Markdown 测试报告，包含摘要、失败分析、用例明细和覆盖率。
  ---
  ```
- [ ] 创建 `package.json`：
  ```json
  {
    "name": "test-report-generation-skill",
    "version": "0.1.0",
    "type": "module",
    "main": "dist/index.js",
    "scripts": {
      "build": "tsc",
      "test": "node --test dist/**/*.test.js"
    },
    "devDependencies": {
      "@types/node": "^20.0.0",
      "typescript": "^5.0.0"
    }
  }
  ```
- [ ] 创建 `tsconfig.json`：
  ```json
  {
    "compilerOptions": {
      "target": "ES2022",
      "module": "ESNext",
      "moduleResolution": "Node",
      "outDir": "./dist",
      "rootDir": "./scripts",
      "strict": true,
      "esModuleInterop": true
    },
    "include": ["scripts/**/*"]
  }
  ```
- [ ] 创建 `scripts/parsers/base.ts`，定义上述所有接口类型
- [ ] 创建 `scripts/index.ts`，导出空的主函数占位

---

## Task 2: 框架检测器

**Files:**
- Create: `skills/test-report-generation/scripts/detector.ts`

**Interfaces:**

```typescript
// 消费：SkillConfig
// 产出：{ framework: string; configFile?: string }

export interface DetectedFramework {
  name: 'jest' | 'vitest' | 'pytest' | 'junit-xml';
  configFile?: string;
  testCommand: string;
}
```

**Steps:**

- [ ] 创建 `scripts/detector.ts`
- [ ] 实现 `detectFramework(projectRoot: string): DetectedFramework | null`
  - 检查顺序：package.json scripts.test → jest.config.* → vitest.config.* → pytest.ini → pyproject.toml
  - 返回检测到的框架名称和建议的测试命令
- [ ] 实现 `detectResultFiles(projectRoot: string, framework: string): string[]`
  - Jest/Vitest: 查找 `test-results.json` 或 `coverage/coverage-final.json`
  - pytest: 查找 `test-results.xml` 或 `pytest-results.xml`
- [ ] 添加单元测试验证检测逻辑

---

## Task 3: Jest/Vitest JSON 解析器

**Files:**
- Create: `skills/test-report-generation/scripts/parsers/jest.ts`
- Create: `skills/test-report-generation/scripts/parsers/vitest.ts`

**Interfaces:**

```typescript
// 消费：TestResultParser, TestResultSummary, TestCaseResult（from base.ts）
// 产出：已解析的 TestResultSummary
```

**Steps:**

- [ ] 创建 `scripts/parsers/jest.ts`
- [ ] 实现 `JestJsonParser implements TestResultParser`
  - `parse(content: string, filePath: string): Promise<TestResultSummary>`
  - 解析 Jest 的 JSON reporter 输出（`testResults` 数组）
  - 提取每个测试用例的 name, status, duration, error
  - 计算汇总统计（total, passed, failed, skipped）
- [ ] 创建 `scripts/parsers/vitest.ts`
- [ ] 实现 `VitestJsonParser implements TestResultParser`
  - Vitest JSON 格式与 Jest 高度兼容，复用解析逻辑
- [ ] 添加示例 JSON 数据用于测试解析正确性

---

## Task 4: JUnit XML 解析器

**Files:**
- Create: `skills/test-report-generation/scripts/parsers/junit-xml.ts`
- Create: `skills/test-report-generation/scripts/parsers/pytest.ts`

**Interfaces:**

```typescript
// 消费：TestResultParser, TestResultSummary（from base.ts）
// 产出：已解析的 TestResultSummary
```

**Steps:**

- [ ] 创建 `scripts/parsers/junit-xml.ts`
- [ ] 实现 `JUnitXmlParser implements TestResultParser`
  - 使用正则表达式解析 XML（避免引入 XML 解析依赖）
  - 解析 `<testsuite>` 和 `<testcase>` 元素
  - 提取 `name`, `classname`, `time`, `<failure>`, `<skipped>` 元素
- [ ] 创建 `scripts/parsers/pytest.ts`
- [ ] 实现 `PytestJUnitParser extends JUnitXmlParser`
  - pytest 的 `--junit-xml` 输出兼容标准 JUnit 格式
  - 添加 pytest 特有字段的处理（如 `file` 属性）
- [ ] 添加示例 XML 数据用于测试

---

## Task 5: 报告生成器（Markdown）

**Files:**
- Create: `skills/test-report-generation/scripts/reporter/markdown.ts`
- Create: `skills/test-report-generation/scripts/reporter/templates.ts`
- Create: `skills/test-report-generation/scripts/utils/format.ts`

**Interfaces:**

```typescript
// 消费：TestResultSummary（from base.ts）
// 产出：string（Markdown 文本）
```

**Steps:**

- [ ] 创建 `scripts/utils/format.ts`
  - 实现 `formatDuration(ms: number): string` → "2m 30s"
  - 实现 `formatPercent(value: number, total: number): string` → "85.5%"
  - 实现 `truncateStack(stack: string, maxLines: number): string`
- [ ] 创建 `scripts/reporter/templates.ts`
  - 定义报告各章节的模板常量
- [ ] 创建 `scripts/reporter/markdown.ts`
- [ ] 实现 `generateMarkdownReport(summary: TestResultSummary): string`
  - **报告头**：项目名、时间、框架、命令
  - **结果摘要**：总数、通过/失败/跳过、通过率、耗时、✅/❌ 标识
  - **失败用例分析**（有失败时）：用例名、文件、错误信息、堆栈摘要
  - **用例明细**：按文件分组，超过 200 条截断
  - **覆盖率**（可选）：语句/分支/函数/行覆盖率表格
  - **附录**：原始文件路径、工具版本
- [ ] 添加测试验证报告格式

---

## Task 6: 测试执行器

**Files:**
- Create: `skills/test-report-generation/scripts/executor.ts`
- Create: `skills/test-report-generation/scripts/utils/file.ts`

**Interfaces:**

```typescript
export interface ExecutionResult {
  success: boolean;
  output: string;
  resultFile?: string;
  error?: string;
}
```

**Steps:**

- [ ] 创建 `scripts/utils/file.ts`
  - 实现 `ensureDir(dir: string): void`
  - 实现 `writeFile(path: string, content: string): void`
- [ ] 创建 `scripts/executor.ts`
- [ ] 实现 `runTests(command: string, projectRoot: string): Promise<ExecutionResult>`
  - 使用 `child_process.spawn` 执行命令
  - 捕获 stdout/stderr
  - 返回执行结果
- [ ] 实现 `generateResults(command: string, format: string): Promise<string>`
  - 对于 Jest/Vitest：追加 `--json --outputFile=test-results.json`
  - 对于 pytest：追加 `--junit-xml=test-results.xml`
- [ ] 处理执行失败情况，返回明确诊断信息

---

## Task 7: 主流程编排

**Files:**
- Modify: `skills/test-report-generation/scripts/index.ts`

**Interfaces:**

```typescript
export async function generateTestReport(config: SkillConfig): Promise<{
  reportPath: string;
  summary: { passed: number; failed: number; passRate: string };
  topErrors?: string[];
}>
```

**Steps:**

- [ ] 在 `scripts/index.ts` 中实现主函数 `generateTestReport`
- [ ] 流程：
  1. 如果 `resultFile` 指定，进入**解析模式**
  2. 否则进入**执行模式**：检测框架 → 执行测试 → 收集结果
  3. 根据结果格式选择解析器
  4. 解析结果生成 `TestResultSummary`
  5. 调用 `generateMarkdownReport` 生成报告
  6. 写入文件到 `outputPath`
  7. 返回报告路径和摘要
- [ ] 实现错误处理：
  - 结果文件不存在或损坏时返回明确错误
  - 解析失败时标注"未获取"并继续其他章节
- [ ] 添加 CLI 入口（解析命令行参数）

---

## Task 8: SKILL.md 完整内容

**Files:**
- Modify: `skills/test-report-generation/SKILL.md`

**Steps:**

- [ ] 补充 SKILL.md 正文内容：
  - **Overview**: 描述 Skill 功能和目标
  - **When to Use**: 触发意图示例（"生成测试报告"、"把这个 junit.xml 转成测试报告"）
  - **Process**: 执行/解析双模式流程图
  - **Common Rationalizations**: 反驳"直接读取原始输出就够了"等错误想法
  - **Red Flags**: 报告为空、摘要与原始输出不一致等异常
  - **Verification**: 生成报告后验证各章节存在且数据一致
- [ ] 添加配置项说明表格
- [ ] 添加示例输出片段

---

## Task 9: 测试与验证

**Files:**
- Create: `skills/test-report-generation/scripts/__tests__/jest.test.ts`
- Create: `skills/test-report-generation/scripts/__tests__/junit-xml.test.ts`
- Create: `skills/test-report-generation/scripts/__tests__/markdown.test.ts`
- Create: `skills/test-report-generation/test-fixtures/jest-sample.json`
- Create: `skills/test-report-generation/test-fixtures/junit-sample.xml`

**Steps:**

- [ ] 创建测试固件目录 `test-fixtures/`
- [ ] 添加 Jest/Vitest JSON 示例数据
- [ ] 添加 JUnit XML 示例数据（含成功、失败、跳过用例）
- [ ] 编写解析器单元测试
- [ ] 编写报告生成器测试
- [ ] 验证 AC1-AC5 验收标准

---

## Verification

```bash
# 构建项目
cd skills/test-report-generation && npm run build

# 运行单元测试
npm test

# 手动验证：使用示例数据生成报告
node dist/index.js --result-file test-fixtures/jest-sample.json --output-path ./reports

# 检查报告结构
cat reports/test-report-*.md
```

---

## Success Criteria

- [ ] AC1: 在含 Jest 或 Vitest 的 TS 项目中执行"生成测试报告"，产出符合 4.2 结构的 Markdown 报告，摘要数据与框架原始输出一致
- [ ] AC2: 存在失败用例时，报告失败分析章节包含用例名、文件路径、错误信息
- [ ] AC3: 提供 JUnit XML 文件走解析模式，不触发测试执行即可产出报告
- [ ] AC4: 结果文件损坏时，Skill 返回明确错误说明而非空报告
- [ ] AC5: 覆盖率数据存在时正确呈现，不存在时标注"未获取"且其余章节正常

---

## Risks & Mitigation

| Risk | Mitigation |
|------|------------|
| R1: 框架 reporter 输出差异大 | 插件式解析器架构，每个框架独立文件 |
| R2: 测试执行耗时不可控 | 支持解析模式，复用已有结果文件 |
| R3: XML 解析无依赖库限制 | 使用正则 + 简单状态机，避免外部依赖 |

---

## Open Questions (已由需求方确认)

- Q1: ✅ 首期目标项目栈以 TypeScript/Node 为主
- Q2: 报告模板默认中文，后续可扩展多语言
- Q3: 本期不做自动推送到 IM/邮件渠道