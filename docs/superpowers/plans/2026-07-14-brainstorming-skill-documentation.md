# Brainstorming Skill 使用文档 - 实施计划

> **使用技能：** writing-plans

## Goal
完成 Brainstorming Skill 使用文档的确认与归档。

## Architecture
该文档已存在于 `.agents/specs/20260714-分析skill并生成使用文档.md`，包含完整的技能说明、执行流程、关键原则和示例场景，无需额外开发工作。

## Tech Stack
- Markdown 文档格式
- Skill 技能体系

---

## File Structure

| 文件路径 | 职责 | 状态 |
|---------|------|------|
| `.agents/specs/20260714-分析skill并生成使用文档.md` | Brainstorming Skill 完整使用文档 | ✅ 已存在 |

---

## Tasks

- [x] **任务完成确认**
  - 文档已包含：概述、核心价值、标准执行流程、关键原则、分阶段指南、视觉伴侣说明、终止状态、自查清单、常见错误、示例场景、技能关系图、总结
  - 文档位置：`.agents/specs/20260714-分析skill并生成使用文档.md`
  - 总计 271 行，内容完整

---

## Summary

**Brainstorming Skill** 核心要点：

1. **强制使用场景** - 创建新功能、构建组件、添加功能逻辑、修改现有行为
2. **执行流程** - 探索项目上下文 → 提出澄清问题 → 提出 2-3 个方案 → 展示设计 → 编写设计文档 → 文档自查 → 用户审查 → 调用 writing-plans
3. **关键原则** - 一次一个问题、多选题优先、YAGNI 严格执行、探索替代方案、增量验证
4. **唯一终止状态** - 必须调用 `writing-plans` 技能过渡到实现阶段

**文档已完整，无需额外工作。**