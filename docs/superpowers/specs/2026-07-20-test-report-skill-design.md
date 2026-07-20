# 测试报告生成 Skill — 需求澄清与设计文档

> 日期：2026-07-20  
> 状态：Draft  
> 阶段：Clarify（需求澄清）  
> 关联需求：任务数量限制 2.0 T3

---

## 1. 需求理解与范围确认

### 1.1 核心问题

测试结果散落在终端/CI 日志/框架原生产物中，缺乏统一的结构化报告。本 Skill 解决：
- 自动收集 → 解析 → 生成标准化报告
- 一条指令完成全流程
- 支持执行模式与解析模式双路径

### 1.2 范围边界（本期 P0）

| 维度 | 本期做 | 本期不做 |
|------|--------|----------|
| 框架 | Jest / Vitest / JUnit XML | Go test, cargo test, pytest（P1） |
| 输出 | Markdown（.md） | HTML, JSON 伴随产物（P1） |
| 功能 | 执行+解析双模式、失败分析、覆盖率展示 | 历史趋势对比、自动推送 IM |
| 交互 | CLI 指令触发 | Web 服务、在线托管 |

---

## 2. 开放问题自动解答（基于上下文推断）

### Q1：首期目标项目栈是否以 TypeScript/Node 为主？

**推断答案：是。**  
依据：需求文档 P0 范围明确 Jest/Vitest 优先，pytest 列为 P1（M2）。且 JUnit XML 作为跨语言兜底格式，表明 TS/Node 是首期主战场。此推断与需求方假设一致。

### Q2：报告是否需要中文/英文双语模板，还是仅中文？

**推断答案：仅中文。**  
依据：需求描述及验收标准均为中文撰写，用户故事角色为中文语境（开发者、QA/测试负责人），无多语言需求提及。后续可扩展为双语但非本期目标。

### Q3：是否需要将报告自动推送到 IM / 邮件等渠道？

**推断答案：否（本期不做）。**  
依据：需求文档 2.2 非目标中已明确列为 Non-Goal。此决策不留悬念。

---

## 3. 设计方案对比

### 方案 A：纯脚本式 Skill（SKILL.md + scripts/）

- **架构**：SKILL.md 定义流程，`scripts/` 目录下放置各框架解析器（Python/Node 脚本），由 Agent 按流程调用
- **优点**：简单直接，与现有 Skill 仓库模式一致，Agent 无需额外运行时依赖
- **缺点**：解析器脚本需维护多语言实现，跨框架复用度低
- **适用**：快速落地，框架类型少时优势明显

### 方案 B：SKILL.md 纯指令式（无 scripts，Agent 自行解析）

- **架构**：SKILL.md 仅描述流程和输出规范，Agent 利用内置能力直接读取结果文件并生成报告
- **优点**：零维护脚本，灵活适应不同框架输出格式
- **缺点**：依赖 Agent 推理质量，复杂 XML/JSON 解析可能不稳定，报告格式一致性差
- **适用**：框架格式简单时可行，但 `JUnit XML` 嵌套结构复杂，纯指令式风险高

### 方案 C：混合式（SKILL.md + 精简 scripts + Agent 编排）

- **架构**：SKILL.md 定义完整流程与模板，scripts 仅提供核心解析器（JUnit XML → 统一中间结构），Agent 负责执行编排、数据填充、模板渲染
- **优点**：解析器精简（只需 1 个通用 JUnit XML parser），Agent 负责灵活编排，兼顾可靠性与可维护性
- **缺点**：需要 1 个 Python 脚本（依赖 `xml.etree` 标准库）

### 推荐方案：C（混合式）

**理由**：
1. JUnit XML 是跨框架兜底格式，也是 Jest/Vitest 可通过 reporter 产出的格式，一次解析覆盖所有 P0 框架
2. 脚本仅负责"解析"这一步，其余（框架检测、命令执行、报告渲染、覆盖率收集）由 Agent 编排，符合 Skill 定位
3. 符合 NFR5 插件式设计：未来新增框架只需新增对应解析器，不影响核心流程
4. 与现有 `test-driven-development` Skill 互补：TDD 负责写测试，本 Skill 负责出报告

---

## 4. 架构设计

### 4.1 整体流程

```
用户指令 → 意图识别 → 模式判断
                      ├── 执行模式：框架检测 → 运行测试 → 收集产物
                      └── 解析模式：定位结果文件
                      ↓
                 结果解析（Parser Layer）
                      ↓
                 数据聚合（中间结构）
                      ↓
                 报告渲染（Markdown 模板）
                      ↓
                 报告落盘 + 摘要回显
```

### 4.2 模块划分

| 模块 | 职责 | 实现方式 |
|------|------|----------|
| **意图识别** | 判断执行/解析模式，提取参数 | Agent 推理（SKILL.md 指导） |
| **框架检测** | 识别测试框架与运行命令 | Agent 按优先级探测（FR1.1） |
| **测试执行** | 运行测试并收集产物 | Agent 调用 shell，后台执行 |
| **结果解析** | 将 JUnit XML / JSON 转为统一中间结构 | `scripts/parse_junit.py`（通用） |
| **覆盖率收集** | 提取 coverage 数据 | Agent 读取 coverage 目录 |
| **报告渲染** | 按 4.2 结构填充 Markdown 模板 | Agent 按模板渲染 |
| **报告落盘** | 写入 `reports/` 目录 | Agent 调用 write |

### 4.3 统一中间结构

```json
{
  "meta": {
    "project": "string",
    "framework": "jest|vitest|pytest|junit",
    "command": "string",
    "timestamp": "ISO8601",
    "duration_ms": 12345
  },
  "summary": {
    "total": 100,
    "passed": 85,
    "failed": 10,
    "skipped": 5,
    "pass_rate": 0.85,
    "conclusion": "pass|fail"
  },
  "failures": [
    {
      "name": "test case name",
      "file": "src/__tests__/foo.test.ts",
      "error_message": "string",
      "stack_trace": ["line1", "line2"]
    }
  ],
  "suites": [
    {
      "file": "src/__tests__/foo.test.ts",
      "duration_ms": 234,
      "cases": [
        {"name": "test A", "status": "passed", "duration_ms": 12}
      ]
    }
  ],
  "coverage": {
    "lines": {"pct": 78.5, "covered": 1000, "total": 1274},
    "branches": {"pct": 65.2, "covered": 150, "total": 230},
    "functions": {"pct": 82.1, "covered": 200, "total": 243},
    "statements": {"pct": 77.8, "covered": 1100, "total": 1414},
    "below_threshold": ["src/untested.ts"]
  }
}
```

### 4.4 文件结构

```
skills/test-report/SKILL.md          # 主流程定义
skills/test-report/scripts/
  parse_junit.py                     # JUnit XML → 中间结构（通用解析器）
  parse_jest_json.py                 # Jest JSON → 中间结构（可选，P1）
  parse_vitest_json.py               # Vitest JSON → 中间结构（可选，P1）
```

---

## 5. 关键设计决策

### 5.1 为什么 JUnit XML 作为核心解析格式？

Jest 和 Vitest 均可通过 reporter 产出 JUnit XML：
- Jest: `jest --reporters=jest-junit` 或 `jest --json` 
- Vitest: `vitest --reporter=junit`

因此，**一个 JUnit XML 解析器即可覆盖 P0 全部框架**，无需分别为 Jest/Vitest 写 JSON 解析器。P1 阶段再补充原生 JSON 解析器以获取更丰富的元数据。

### 5.2 执行模式下的测试命令策略

按 FR1.1 优先级：
1. 用户显式指定（`test_command` 配置项）
2. 项目配置检测（`package.json` scripts.test → 推断框架 → 追加 reporter 参数）
3. 框架特征文件推断（`jest.config.*` → `npx jest --json --outputFile=...`）

### 5.3 覆盖率数据获取策略

- `coverage=auto`（默认）：检测 `coverage/` 目录下是否存在 `coverage-summary.json` 或 `lcov.info`，存在则解析
- `coverage=on`：强制要求覆盖率数据，缺失则标注"未获取"并警告
- `coverage=off`：跳过覆盖率章节
- 覆盖率阈值：`fail_threshold` 配置项控制，低于阈值时报告结论标记为 ❌ 不达标

### 5.4 敏感信息过滤

按 NFR3 要求，报告渲染阶段过滤：
- 环境变量名含 `SECRET`、`KEY`、`TOKEN`、`PASSWORD` 的值替换为 `***`
- 堆栈中的绝对路径截断为用户名/项目名以外部分
- 不记录原始命令行中的完整环境变量

---

## 6. 风险与缓解

| 风险 | 等级 | 缓解措施 |
|------|------|----------|
| JUnit XML 格式差异（不同框架产出结构不同） | 中 | 解析器使用宽松容错模式，字段缺失标注"未获取" |
| 测试执行超时（R2） | 高 | 利用 `run_in_background` + `background_exec` 轮询，超时后报告已收集结果 |
| Vitest 默认不输出 JUnit XML | 低 | Skill 自动追加 `--reporter=junit --outputFile=...` 参数 |
| 覆盖率数据格式不统一 | 中 | 优先 `coverage-summary.json`（标准格式），降级解析 `lcov.info` |

---

## 7. 验收标准映射

| AC | 对应设计 | 验证方式 |
|-----|----------|----------|
| AC1: Jest/Vitest → Markdown 报告 | 执行模式 → JUnit XML 解析 → 模板渲染 | 端到端测试 |
| AC2: 失败用例含用例名/文件/错误 | 中间结构 `failures[]` → 报告第 3 章 | 失败用例注入验证 |
| AC3: JUnit XML 解析模式不触发执行 | 意图识别 → 解析模式分支 | 提供 XML 文件验证 |
| AC4: 损坏文件返回错误 | 解析器异常处理 → 明确错误信息 | 损坏 XML 输入验证 |
| AC5: 覆盖率不存在时标注"未获取" | `coverage=auto` 降级逻辑 | 无覆盖率项目验证 |

---

## 8. 下一步

1. **进入 PLAN 阶段**：调用 `planning-and-task-breakdown` 技能，将设计拆分为 M1/M2/M3 可执行任务
2. **M1 实现**：SKILL.md 编写 + `parse_junit.py` 脚本 + Markdown 模板
3. **验证**：在含 Jest/Vitest 的 TS 项目中端到端验证

---

## 附录：需求文档开放问题决策记录

| 问题 | 决策 | 理由 |
|------|------|------|
| Q1: 首期以 TS/Node 为主？ | ✅ 是 | P0 范围为 Jest/Vitest，需求方假设一致 |
| Q2: 中文/英文双语？ | 仅中文 | 需求文档及用户故事均为中文语境 |
| Q3: 推送到 IM/邮件？ | 本期不做 | 需求文档 2.2 非目标已明确排除 |