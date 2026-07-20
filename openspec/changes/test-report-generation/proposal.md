# Proposal: test-report-generation

## Summary
创建一个 Skill，Agent 在执行测试后能够自动解析测试结果并生成结构化、可读性强的标准测试报告。

## Motivation
当前团队在完成测试执行后，测试结果散落在终端输出、CI 日志或框架原生产物中，存在以下痛点：
- 测试结果需要人工收集、整理、汇总，耗时且易遗漏；
- 缺乏统一格式的测试报告，跨项目/跨团队沟通成本高；
- 失败用例的上下文需要人工回溯；
- 覆盖率、通过率等质量指标无法沉淀为可追踪的历史数据。

## Goals
- G1：一条指令即可自动完成：执行测试 → 收集结果 → 生成报告
- G2：报告内容标准化，包含摘要、明细、失败分析、覆盖率四大板块
- G3：支持主流测试框架的结果解析（Jest、Vitest、pytest、JUnit XML）
- G4：报告支持多种输出格式，默认 Markdown

## Non-Goals
- 不做测试用例的自动生成或修复
- 不做报告的在线托管 / Web 服务化展示
- 不做多次运行结果的趋势对比分析
- 不做非测试类质量报告的聚合

## Scope
- 新增 Skill：`skills/test-report-generation/SKILL.md`
- 新增脚本：解析工具脚本（Python）
- 支持两种模式：执行模式（运行测试+生成报告）和解析模式（仅解析已有结果）

## Success Criteria
- AC1：在含 Jest 或 Vitest 的 TS 项目中产出符合结构的 Markdown 报告
- AC2：存在失败用例时，报告包含失败分析章节
- AC3：提供 JUnit XML 文件走解析模式，不触发测试执行即可产出报告
- AC4：结果文件损坏时，返回明确错误说明
- AC5：覆盖率数据存在时正确呈现，不存在时标注"未获取"

## Risks
- R1：各框架 reporter 输出差异大，需要良好抽象
- R2：测试执行耗时不可控

## Milestones
- M1 (P0)：Jest/Vitest JSON + JUnit XML 解析、Markdown 报告、执行/解析双模式
- M2 (P1)：pytest 支持、覆盖率章节、fail_threshold
- M3 (P1)：HTML 输出、JSON 伴随产物
- M4 (P2)：历史趋势对比、更多框架