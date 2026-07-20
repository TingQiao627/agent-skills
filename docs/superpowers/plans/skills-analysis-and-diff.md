# Skills 仓库分析 & main 分支差异报告

> **For agentic workers:** 本文档为分析报告，非实施计划。记录了仓库中 24 个 Skill 的功能分析及与 main 分支的差异。

**Goal:** 分析 `skills/` 目录下每个 Skill 的功能定位，并对比当前分支与 main 分支的差异。

**Architecture:** 本仓库是一个 Agent Skills 集合，面向高级软件工程师。每个 Skill 是 `skills/<kebab-case-name>/SKILL.md` 的 Markdown 文件，包含 YAML frontmatter（name, description）和结构化工作流指导。

**Tech Stack:** Markdown + YAML frontmatter，部分 Skill 含 scripts/ 目录。

---

## 与 main 分支的差异

### Git Diff 统计

```
git diff --stat main
 test.txt | 5 +----
 1 file changed, 1 insertion(+), 4 deletions(-)
```

### 当前分支领先 main 的提交

```
git log --oneline main..HEAD
 01bdfcd test
```

**结论：当前分支与 main 分支几乎一致，仅 `test.txt` 有 1 行新增、4 行删除的微小变更，属于测试性修改。所有 24 个 Skill 与 main 分支无差异。**

---

## Skill 功能全景分析

### 按开发阶段分类

| 阶段 | Skill |
|------|-------|
| **定义 (DEFINE)** | interview-me, idea-refine, spec-driven-development, doubt-driven-development |
| **规划 (PLAN)** | planning-and-task-breakdown, writing-plans (managed) |
| **构建 (BUILD)** | incremental-implementation, test-driven-development, frontend-ui-engineering, api-and-interface-design, source-driven-development, context-engineering |
| **验证 (VERIFY)** | debugging-and-error-recovery, code-review-and-quality, browser-testing-with-devtools, security-and-hardening, performance-optimization, observability-and-instrumentation |
| **交付 (SHIP)** | shipping-and-launch, git-workflow-and-versioning, ci-cd-and-automation |
| **横切关注点** | code-simplification, deprecation-and-migration, documentation-and-adrs, using-agent-skills |

---

## 逐 Skill 详解

### 1. interview-me
- **描述:** 在编写任何计划/规格/代码之前，通过一问一答的方式挖掘用户真正想要的东西，而非他们嘴上说的。
- **触发条件:** 需求不明确（缺少 who/why/success/constraint）、用户说"interview me"/"grill me"等。
- **开发阶段:** DEFINE（最前置）

### 2. idea-refine
- **描述:** 通过结构化发散-收敛思维，将模糊想法打磨成清晰可执行的概念。
- **触发条件:** "ideate"、"refine this idea"、"stress-test my plan"。
- **开发阶段:** DEFINE

### 3. spec-driven-development
- **描述:** 在编码前创建结构化规格说明，作为人与 Agent 之间的共享真相源。
- **工作流:** SPECIFY → PLAN → TASKS → IMPLEMENT，每阶段需人工确认。
- **开发阶段:** DEFINE

### 4. doubt-driven-development
- **描述:** 在非平凡决策落地前，引入一个"偏向证伪"的审查视角进行交叉检验。
- **适用场景:** 涉及分支逻辑、跨模块边界、类型系统无法验证的属性、不可逆操作。
- **开发阶段:** DEFINE（防御性）

### 5. planning-and-task-breakdown
- **描述:** 将工作拆分为小型、可验证、带明确验收标准的任务。
- **流程:** 进入 Plan Mode → 分析依赖 → 分解任务 → 输出有序任务列表。
- **开发阶段:** PLAN

### 6. incremental-implementation
- **描述:** 以薄垂直切片方式增量构建 —— 实现一块、测试、验证、提交，再扩展。
- **核心循环:** Implement → Test → Verify → Commit → 重复。
- **开发阶段:** BUILD

### 7. test-driven-development
- **描述:** 红-绿-重构循环：先写失败测试，再写最小实现代码使其通过，最后重构。
- **Bug 修复:** Prove-It Pattern —— 先用测试复现 Bug。
- **开发阶段:** BUILD

### 8. frontend-ui-engineering
- **描述:** 构建可访问、高性能、视觉精致的生产级 UI，避免"AI 审美"。
- **关注点:** 组件架构、设计系统、可访问性、响应式、交互模式。
- **开发阶段:** BUILD

### 9. api-and-interface-design
- **描述:** 设计稳定、文档完善、难以误用的接口（REST、GraphQL、模块边界、组件 Props）。
- **核心原则:** Hyrum's Law —— 所有可观察行为最终都会被依赖。
- **开发阶段:** BUILD

### 10. source-driven-development
- **描述:** 每个框架特定代码决策必须由官方文档支撑。不凭记忆编码。
- **流程:** DETECT → FETCH → IMPLEMENT → CITE。
- **开发阶段:** BUILD

### 11. context-engineering
- **描述:** 为 Agent 精心策划上下文 —— 在正确的时间提供正确的信息。
- **上下文层次:** Rules Files → Spec/Architecture Docs → Relevant Source Files。
- **开发阶段:** BUILD（辅助）

### 12. debugging-and-error-recovery
- **描述:** 结构化根本原因调试，包含 Stop-the-Line 规则。
- **流程:** STOP → PRESERVE → DIAGNOSE → FIX → GUARD。
- **开发阶段:** VERIFY

### 13. code-review-and-quality
- **描述:** 多维度代码审查，覆盖五轴：正确性、可读性、架构、安全性、性能。
- **审批标准:** 只要变更明确改善代码整体健康度即可批准，不追求完美。
- **开发阶段:** VERIFY

### 14. browser-testing-with-devtools
- **描述:** 通过 Chrome DevTools MCP 在真实浏览器中进行测试，检查 DOM、控制台、网络、性能。
- **前置条件:** 需配置 chrome-devtools MCP server。
- **开发阶段:** VERIFY

### 15. security-and-hardening
- **描述:** 安全优先的开发实践 —— 威胁建模 → 输入验证 → 认证授权 → 数据保护。
- **流程:** 先用 STRIDE 做威胁建模，再施加控制。
- **开发阶段:** VERIFY

### 16. performance-optimization
- **描述:** 先测量再优化。以 Core Web Vitals (LCP/INP/CLS) 为目标进行性能调优。
- **原则:** 不猜测，不提前优化。
- **开发阶段:** VERIFY

### 17. observability-and-instrumentation
- **描述:** 在功能开发同时编写遥测代码（日志、指标、追踪、告警），使生产行为可观测。
- **原则:** 没有遥测的功能上线后第一个 Bug 变成考古学。
- **开发阶段:** VERIFY

### 18. shipping-and-launch
- **描述:** 安全上线 —— 预发布检查清单、监控、分阶段发布、回滚策略。
- **检查清单:** 代码质量、监控、发布策略、回滚计划。
- **开发阶段:** SHIP

### 19. git-workflow-and-versioning
- **描述:** 基于 Trunk-Based Development 的 Git 工作流，含语义化版本和变更日志。
- **原则:** 短期分支（1-3天）、原子提交、描述性消息。
- **开发阶段:** SHIP

### 20. ci-cd-and-automation
- **描述:** 自动化质量门禁 —— lint → test → build → deploy，左移发现问题。
- **原则:** 越快越安全（小批量频繁发布降低风险）。
- **开发阶段:** SHIP

### 21. code-simplification
- **描述:** 在保持行为不变的前提下简化代码，降低认知负担。
- **五大原则:** 可读性、减少嵌套、消除重复、命名清晰、单一职责。
- **开发阶段:** 横切（重构）

### 22. deprecation-and-migration
- **描述:** 管理代码生命周期 —— 代码是负债而非资产，安全地移除旧系统。
- **核心原则:** Hyrum's Law 使移除变得困难，需规划迁移路径。
- **开发阶段:** 横切（生命周期）

### 23. documentation-and-adrs
- **描述:** 记录决策而非代码 —— ADR 捕获"为什么这样构建"及被考虑的替代方案。
- **原则:** 不记录显而易见的代码，不写一次性原型的文档。
- **开发阶段:** 横切（知识管理）

### 24. using-agent-skills
- **描述:** 元技能 —— 根据任务所处的开发阶段，发现并调用正确的 Skill。
- **决策树:** 从 "不知道要什么?" → interview-me 到 "实现代码?" → incremental-implementation。
- **开发阶段:** 横切（编排）

---

## 与 AGENTS.md 中定义的 Skill 映射关系

AGENTS.md 定义了以下生命周期映射，与上述 Skill 对应：

| 生命周期阶段 | 对应 Skill |
|-------------|-----------|
| DEFINE | spec-driven-development, interview-me, idea-refine, doubt-driven-development |
| PLAN | planning-and-task-breakdown |
| BUILD | incremental-implementation, test-driven-development |
| VERIFY | debugging-and-error-recovery |
| REVIEW | code-review-and-quality |
| SHIP | shipping-and-launch |

此外，AGENTS.md 还定义了 **Personas**（`agents/*.md`）和 **Slash Commands**（`.claude/commands/*.md`）两层编排机制，与 Skills 组成三层架构。

---

## 变更风险评估

| 风险 | 等级 | 说明 |
|------|------|------|
| 当前分支与 main 差异 | 无风险 | 仅 test.txt 测试性修改 |
| Skill 文件完整性 | 无风险 | 24 个 Skill 均有完整 SKILL.md |

---

## 验证

- [x] `git diff --stat main` 确认仅 test.txt 变更
- [x] `git log main..HEAD` 确认仅 1 个提交
- [x] 24 个 Skill 目录均含 SKILL.md 且 frontmatter 完整
- [x] Skill 分类与 AGENTS.md 生命周期映射一致