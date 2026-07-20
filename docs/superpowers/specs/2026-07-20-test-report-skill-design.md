# 测试报告生成 Skill — 设计文档

> 创建日期: 2026-07-20
> 状态: 需求澄清 / 设计阶段
> 关联需求: 任务数量限制2.0 T2

---

## 1. 问题陈述 (Problem Statement)

当前团队测试执行后，结果散落在终端输出、CI 日志或框架原生产物中，存在以下痛点：

- 测试结果需人工收集、整理、汇总，耗时且易遗漏
- 缺乏统一格式的测试报告，跨项目/跨团队沟通成本高
- 失败用例的上下文（错误信息、堆栈、关联代码）需人工回溯
- 覆盖率、通过率等质量指标无法沉淀为可追踪的历史数据

**核心诉求**：一个 Agent 可调用的 Skill，在测试执行后自动解析结果并生成结构化、可读性强的标准测试报告。

---

## 2. 目标与非目标 (Goals & Non-Goals)

### 2.1 目标

| 编号 | 目标 | 衡量标准 |
|------|------|----------|
| G1 | 一条指令自动完成「执行→收集→报告」 | 用户说"生成测试报告"即可获得完整报告 |
| G2 | 报告内容标准化 | 含摘要、明细、失败分析、覆盖率四大板块 |
| G3 | 支持主流测试框架 | Jest、Vitest、pytest、JUnit XML（详见 §3） |
| G4 | 多格式输出 | 默认 Markdown，P1 支持 HTML + JSON 伴随产物 |

### 2.2 非目标（本期不做）

- 不做测试用例的自动生成或修复（仅报告）
- 不做报告的在线托管 / Web 服务化展示
- 不做多次运行结果的趋势对比分析（列为 P2 后续迭代）
- 不做非测试类质量报告（如 lint、安全扫描）的聚合

---

## 3. 设计方案对比 (Design Alternatives)

### 方案 A：纯脚本式 Skill（推荐 ✅）

**思路**：Skill 主体为 Markdown 指令 + 一个核心报告生成脚本（Python/Node），Agent 按步骤调用脚本完成解析与生成。

| 维度 | 评价 |
|------|------|
| 复杂度 | 低 — 单一脚本，无外部依赖 |
| 可维护性 | 高 — 解析器按框架分模块，插件式扩展 |
| 兼容性 | 好 — 脚本可在 Agent 沙箱中直接执行 |
| 与现有 Skill 一致 | ✅ 符合 `skills/<name>/SKILL.md` + `scripts/` 模式 |
| 风险 | 脚本需在 Agent 环境中可用（Python 3.8+ 普遍可用） |

### 方案 B：纯 Markdown 指令式 Skill

**思路**：SKILL.md 仅为自然语言指令，Agent 自行调用框架 CLI 并手工解析输出。

| 维度 | 评价 |
|------|------|
| 复杂度 | 极低 — 无脚本 |
| 可维护性 | 低 — 解析逻辑依赖 Agent 的 LLM 推理，一致性差 |
| 可靠性 | 差 — 每次生成报告格式可能不同，依赖 LLM 能力 |
| 优势 | 零依赖，跨所有环境 |

### 方案 C：多脚本 + 独立 NPM/PyPI 包

**思路**：报告生成作为独立工具包发布，Skill 仅做薄封装。

| 维度 | 评价 |
|------|------|
| 复杂度 | 高 — 需维护独立包、发布流程 |
| 优势 | 可脱离 Agent 独立使用 |
| 劣势 | 过度工程化，与 Skill 定位不符 |

### 决策

**选择方案 A**。理由：
1. 与现有 repo 中 `skills/` 的脚本扩展模式一致（如 `browser-testing-with-devtools/` 含脚本）
2. 解析逻辑确定性高，不受 LLM 推理波动影响
3. 复杂度可控，Python 脚本是 Agent 沙箱的通用能力
4. 插件式解析器架构天然支持扩展（NFR5）

---

## 4. 架构设计 (Architecture)

### 4.1 整体流程

```
用户指令 → Skill 触发
              │
              ▼
      ┌─ 模式判定 ─────────────┐
      │                        │
  执行模式                  解析模式
      │                        │
      ▼                        ▼
  自动检测框架         用户指定结果文件
  执行测试命令         解析已有产物
      │                        │
      ▼                        ▼
      └────── 结果收集 ────────┘
                  │
                  ▼
          ┌─ 解析器路由 ─┐
          │   (按格式)    │
          ├───────────────┤
          │ Jest/Vitest   │
          │   JSON Parser │
          ├───────────────┤
          │ pytest        │
          │   JUnit XML   │
          ├───────────────┤
          │ Generic       │
          │   JUnit XML   │
          └───────────────┘
                  │
                  ▼
           报告生成引擎
                  │
        ┌─────────┼─────────┐
        ▼         ▼         ▼
    Markdown    HTML      JSON
    (默认)     (P1)     (伴随)
                  │
                  ▼
            落盘 + 摘要返回
```

### 4.2 目录结构

```
skills/test-report/
├── SKILL.md                    # Skill 入口指令
├── scripts/
│   ├── generate_report.py      # 主入口：模式判定 → 执行/解析 → 生成
│   ├── parsers/
│   │   ├── __init__.py
│   │   ├── base.py             # 解析器基类 + 插件注册
│   │   ├── jest_vitest.py      # Jest/Vitest JSON reporter
│   │   ├── pytest_junit.py     # pytest JUnit XML
│   │   └── junit_xml.py        # 通用 JUnit XML
│   ├── reporters/
│   │   ├── __init__.py
│   │   ├── markdown.py         # Markdown 报告生成
│   │   ├── html.py             # HTML 报告生成 (P1)
│   │   └── json_reporter.py    # JSON 伴随产物
│   └── templates/
│       ├── report.md.j2        # Markdown 模板
│       └── report.html.j2      # HTML 模板 (P1)
```

### 4.3 解析器插件架构

遵循 NFR5「插件式结构，新增框架支持不影响既有解析器」：

```python
# parsers/base.py — 抽象基类
class BaseParser(ABC):
    @abstractmethod
    def can_parse(self, file_path: str) -> bool: ...
    @abstractmethod
    def parse(self, file_path: str) -> TestReport: ...

# 自动发现：遍历 parsers/ 目录下所有 BaseParser 子类
# 新增框架：只需添加一个 parsers/<framework>.py 文件
```

---

## 5. 报告内容结构 (Report Schema)

### 5.1 结构化数据模型

```python
@dataclass
class TestReport:
    meta: ReportMeta           # 报告头
    summary: ResultSummary     # 结果摘要
    failures: list[Failure]    # 失败用例分析
    suites: list[TestSuite]    # 用例明细（按文件分组）
    coverage: Coverage | None  # 覆盖率（可空）
    appendix: Appendix         # 附录

@dataclass
class ReportMeta:
    project_name: str
    generated_at: str          # ISO 8601
    test_command: str
    framework: str             # jest / vitest / pytest / junit
    framework_version: str
    environment: str           # os + python/node version

@dataclass
class ResultSummary:
    total: int
    passed: int
    failed: int
    skipped: int
    pass_rate: float           # 0.0 - 1.0
    duration_secs: float
    conclusion: str            # "✅ PASS" / "❌ FAIL"
    fail_threshold: float | None  # 达标阈值

@dataclass
class Failure:
    name: str
    file_path: str
    error_message: str
    stack_trace: str           # 截断至可读长度 (≤20行)
    duration_secs: float

@dataclass
class TestSuite:
    file_path: str
    total: int
    passed: int
    failed: int
    skipped: int
    duration_secs: float
    cases: list[TestCase]

@dataclass
class TestCase:
    name: str
    status: str                # passed / failed / skipped
    duration_secs: float

@dataclass
class Coverage:
    statement_pct: float | None
    branch_pct: float | None
    function_pct: float | None
    line_pct: float | None
    low_coverage_files: list[CoverageFile]  # 低于阈值文件

@dataclass
class CoverageFile:
    path: str
    statement_pct: float
    branch_pct: float
    function_pct: float
    line_pct: float

@dataclass
class Appendix:
    source_files: list[str]    # 原始结果文件路径
    tool_version: str
```

### 5.2 Markdown 报告章节顺序

1. **报告头** — 项目名、生成时间、执行命令、框架/版本、环境摘要
2. **结果摘要** — 用例总数、通过/失败/跳过数、通过率、总耗时、✅/❌ 结论
3. **失败用例分析** — 每条失败：用例名、文件路径、错误信息、堆栈关键行
4. **用例明细** — 按文件分组，超 200 条截断并注明
5. **覆盖率** — 语句/分支/函数/行覆盖率总表 + 低覆盖率文件清单
6. **附录** — 原始结果文件路径、生成工具版本

---

## 6. SKILL.md 交互约定

### 6.1 触发意图

| 用户输入示例 | 模式 | 说明 |
|-------------|------|------|
| "生成测试报告" | 执行模式 | 自动检测框架并运行测试 |
| "跑一下测试并出报告" | 执行模式 | 同上 |
| "把这个 junit.xml 转成测试报告" | 解析模式 | 指定结果文件，跳过执行 |
| "用 pytest --coverage 跑测试后生成报告" | 执行模式 | 用户显式指定命令 |

### 6.2 可配置项

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `test_command` | 自动检测 | 测试执行命令 |
| `result_file` | 自动检测 | 解析模式下的结果文件路径 |
| `output_format` | `markdown` | `markdown` / `html` / `json` |
| `output_path` | `reports/` | 报告输出目录 |
| `coverage` | `auto` | `auto` / `on` / `off` |
| `fail_threshold` | 无 | 通过率低于该值时标记不达标 |

### 6.3 框架检测优先级

1. 用户显式指定的命令
2. `package.json` scripts (`test`)、`pyproject.toml`、`Cargo.toml` 等项目配置
3. 框架特征文件推断（`jest.config.*`、`vitest.config.*`、`pytest.ini`）

---

## 7. 开放问题决策 (Open Questions — 自主判定)

根据防阻塞协议，对需求文档中列出的开放问题做出如下自主决策：

| 问题 | 决策 | 理由 |
|------|------|------|
| **Q1**: 首期目标项目栈是否以 TS/Node 为主？ | ✅ **是**。P0 范围按 Jest/Vitest + JUnit XML 制定 | 需求文档已按此假设制定 P0 范围；Jest/Vitest 是当前前端生态最主流框架 |
| **Q2**: 报告是否需要中/英文双语模板？ | **默认中文**，P1 支持 `--lang en` 切换英文模板 | 需求文档为中文，主要用户群体为中文团队；英文模板通过模板参数化实现，成本低 |
| **Q3**: 是否需要推送 IM/邮件？ | ❌ **不做** | 需求文档已明确列为非目标，且涉及外部 API 集成和权限管理，超出当前范围 |

### 额外判定的隐性决策

| 决策点 | 判定 | 理由 |
|--------|------|------|
| 报告生成脚本语言 | **Python 3.8+** | Agent 沙箱通用能力；XML/JSON 解析生态成熟（`xml.etree`、`json` 标准库）；Jinja2 模板可选依赖 |
| 覆盖率数据来源 | 优先使用框架内置 coverage reporter（如 Jest `--coverage`、pytest-cov），解析其 JSON/XML 输出 | 避免重复实现覆盖率收集逻辑 |
| 命名规范 | `test-report`（kebab-case） | 与现有 skills 目录命名一致 |
| 报告默认输出路径 | `reports/test-report-<YYYYMMDD-HHmmss>.md` | 需求文档明确指定 |

---

## 8. 里程碑规划 (Milestones)

| 阶段 | 范围 | 优先级 | 预估产出 |
|------|------|--------|----------|
| **M1** | Jest/Vitest JSON + JUnit XML 解析、Markdown 报告、执行/解析双模式 | **P0** | SKILL.md + `generate_report.py` + `parsers/jest_vitest.py` + `parsers/junit_xml.py` + `reporters/markdown.py` |
| **M2** | pytest 支持、覆盖率章节、`fail_threshold` | **P1** | `parsers/pytest_junit.py` + 覆盖率集成 + 阈值逻辑 |
| **M3** | HTML 输出、JSON 伴随产物、英文模板 | **P1** | `reporters/html.py` + `reporters/json_reporter.py` + `templates/report.html.j2` + i18n 支持 |
| **M4** | 历史趋势对比、更多框架（Go test / cargo test） | **P2** | 后续迭代 |

---

## 9. 风险与缓解 (Risks & Mitigations)

| 风险 | 等级 | 缓解措施 |
|------|------|----------|
| **R1**: 各框架 reporter 输出差异大 | 中 | 解析器插件式架构（NFR5），每框架独立解析器，互不影响 |
| **R2**: 测试执行耗时不可控 | 中 | 长任务交由 Agent 后台执行 (`run_in_background`)，Skill 轮询结果 |
| **R3**: 结果文件格式异常/字段缺失 | 低 | 降级策略：缺失项标注"未获取"，不崩溃不丢数据（NFR2） |
| **R4**: 敏感信息泄露 | 中 | 堆栈过滤：屏蔽环境变量值、密钥模式（`AWS_*`、`TOKEN`、`SECRET`）；路径脱敏（NFR3） |
| **R5**: Agent 沙箱缺少 Python | 低 | 提供降级到纯 Markdown 指令的 fallback 路径 |

---

## 10. 验收标准映射 (Acceptance Criteria Mapping)

| AC | 描述 | 验证方式 |
|----|------|----------|
| AC1 | Jest/Vitest 项目生成 Markdown 报告，摘要与框架输出一致 | 在含 Jest/Vitest 的 TS 项目中执行 Skill，对比报告摘要与框架原始输出 |
| AC2 | 失败用例含用例名、文件路径、错误信息 | 构造含失败用例的测试套件，检查报告失败分析章节 |
| AC3 | JUnit XML 解析模式不触发测试执行 | 提供 JUnit XML 文件，验证 Skill 跳过 `test_command` 执行 |
| AC4 | 结果文件损坏返回明确错误 | 提供格式错误的 XML/JSON，验证 Skill 返回错误说明而非空报告 |
| AC5 | 覆盖率存在时正确呈现，不存在时标注"未获取" | 分别在有/无覆盖率数据场景下验证报告完整性 |

---

## 11. 与现有 Skill 的关系

| 现有 Skill | 关系 | 说明 |
|------------|------|------|
| `test-driven-development` | **互补** | TDD Skill 负责「写测试→实现→重构」循环；本 Skill 负责「执行→收集→报告」产出 |
| `spec-driven-development` | **上游** | 需求定义阶段使用 |
| `planning-and-task-breakdown` | **上游** | 设计完成后进入任务拆分 |
| `incremental-implementation` | **下游** | 按里程碑逐步实现 |

---

## 12. 下一步行动

1. **本阶段产出已完成**：本设计文档即为 clarify 阶段产物
2. **下一阶段**：进入 `planning-and-task-breakdown` → 将 M1 拆分为可执行任务
3. **实现入口**：先创建 `skills/test-report/SKILL.md`，再按 M1 范围实现脚本

---

*Design doc generated by brainstorming skill. Ready for review and transition to planning.*