# Proposal: test-report-generator

## Change ID
`test-report-generator`

## Why
当前团队在完成测试执行后，测试结果散落在终端输出、CI 日志或框架原生产物（如 JUnit XML、coverage 目录）中，存在以下痛点：

- 测试结果需要人工收集、整理、汇总，耗时且易遗漏；
- 缺乏统一格式的测试报告，跨项目/跨团队沟通成本高；
- 失败用例的上下文（错误信息、堆栈、关联代码）需要人工回溯；
- 覆盖率、通过率等质量指标无法沉淀为可追踪的历史数据。

## What
提供一个 Skill，Agent 在执行测试后能够自动解析测试结果并生成结构化、可读性强的标准测试报告。核心能力：

1. **执行模式**：一条指令即可自动完成"执行测试 → 收集结果 → 生成报告"；
2. **解析模式**：跳过执行，直接解析已有结果文件（JUnit XML / JSON）生成报告；
3. **标准化报告**：报告内容包含摘要、明细、失败分析、覆盖率四大板块；
4. **多格式输出**：默认 Markdown，P1 支持 HTML 和 JSON 伴随产物；
5. **多框架支持**：Jest、Vitest、pytest、JUnit XML（通用兜底）。

## Scope
- **In scope**：
  - 测试执行与结果收集（自动识别框架与运行命令）
  - 结果解析（Jest/Vitest JSON、pytest JUnit XML/JSON、JUnit XML 通用）
  - 报告生成（Markdown 默认，HTML/JSON P1）
  - 覆盖率数据提取（auto/on/off 可配置）
  - 可配置项：test_command、result_file、output_format、output_path、coverage、fail_threshold
- **Out of scope (本期不做)**：
  - 测试用例自动生成或修复
  - 报告在线托管 / Web 服务化展示
  - 多次运行结果的趋势对比分析
  - 非测试类质量报告（lint、安全扫描）聚合

## Affected Areas
- 新增 `skills/test-report-generator/` 目录（SKILL.md + scripts/）
- 不影响仓库现有结构的其他部分

## Risks
| 风险 | 缓解措施 |
|------|----------|
| 各框架 reporter 输出差异大，解析层复杂 | 采用插件式解析架构（NFR5），每个框架独立解析器 |
| 测试执行耗时不可控 | 依赖 Agent 运行时的后台任务能力，长任务轮询 |
| 覆盖率数据格式不统一 | 覆盖率标注为 auto/on/off 可配置，缺失时降级输出"未获取" |

## Rollout
- 作为独立 Skill 发布，不影响现有 Skills
- 无 breaking changes，无迁移需求

## Open Questions (需求方确认)
- Q1：首期目标项目栈是否以 TypeScript/Node 为主？—— 本文档按此假设制定 P0 范围
- Q2：报告是否需要中文/英文双语模板，还是仅中文？—— 当前按中文模板设计
- Q3：是否需要将报告自动推送到 IM / 邮件等渠道？—— 当前列为非目标