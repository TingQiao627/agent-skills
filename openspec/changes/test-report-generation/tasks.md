# Tasks: test-report-generation

## Task List

### T1: 创建 Skill 骨架 (SKILL.md)
- 状态: pending
- 优先级: high
- 描述: 创建 `skills/test-report-generation/SKILL.md`，包含 YAML frontmatter、Overview、When to Use、Process 等标准章节

### T2: 实现结果解析器脚本
- 状态: pending
- 优先级: high
- 描述: 创建 Python 脚本实现 JUnit XML、Jest JSON、Vitest JSON 的结果解析
- 产出: `skills/test-report-generation/scripts/parse_results.py`

### T3: 实现报告生成器脚本
- 状态: pending
- 优先级: high
- 描述: 创建 Python 脚本根据解析结果生成 Markdown 报告
- 产出: `skills/test-report-generation/scripts/generate_report.py`

### T4: 实现测试框架自动检测
- 状态: pending
- 优先级: high
- 描述: 在 SKILL.md 中描述自动检测逻辑，支持 package.json、jest.config.*、vitest.config.*、pytest.ini 等

### T5: 实现执行模式与解析模式
- 状态: pending
- 优先级: high
- 描述: 在 Skill 流程中实现两种模式：自动执行测试 + 收集结果 vs 直接解析已有结果文件

### T6: 覆盖率解析与展示
- 状态: pending
- 优先级: medium
- 描述: 支持解析覆盖率数据（Jest/Vitest coverage、pytest-cov），在报告中展示覆盖率章节

### T7: 验证与测试
- 状态: pending
- 优先级: medium
- 描述: 使用示例数据验证脚本功能正确性，确保报告格式符合需求