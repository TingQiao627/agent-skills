# Tasks: 测试报告生成 Skill

## M1：P0 核心功能（Jest/Vitest + JUnit XML + Markdown）

- [ ] 1. 创建 Skill 目录结构 `skills/test-report-generator/`，编写 `SKILL.md` 入口
- [ ] 2. 实现框架检测器 `detect-framework.ts`：自动识别 Jest / Vitest / pytest / JUnit XML
- [ ] 3. 实现 Jest JSON 解析器 `jest-parser.ts`：解析 `--json --outputFile` 产物
- [ ] 4. 实现 Vitest JSON 解析器 `vitest-parser.ts`：解析 `--reporter=json` 产物
- [ ] 5. 实现 JUnit XML 解析器 `junit-parser.ts`：通用 XML 解析，作为兜底
- [ ] 6. 实现 Parser Registry：插件注册与自动选择
- [ ] 7. 实现测试执行器 `run-test.ts`：封装执行模式逻辑
- [ ] 8. 实现报告构建器 `report-builder.ts`：将标准化结果组装为报告章节
- [ ] 9. 实现 Markdown 输出器 `md-writer.ts`：生成 FR2 结构的 Markdown 文件
- [ ] 10. 创建 Markdown 报告模板 `templates/report-template.md`
- [ ] 11. 实现双模式路由：执行模式 / 解析模式
- [ ] 12. 编写单元测试：各解析器正确性验证
- [ ] 13. 编写集成测试：端到端报告生成流程

## M2：P1 扩展（pytest + 覆盖率 + fail_threshold）

- [ ] 14. 实现 pytest 解析器 `pytest-parser.ts`：支持 JUnit XML 与 JSON report
- [ ] 15. 实现覆盖率采集器 `coverage-collector.ts`：istanbul / pytest-cov 数据解析
- [ ] 16. 报告构建器增加覆盖率章节
- [ ] 17. 实现 `fail_threshold` 逻辑：低于阈值时标记结论为不达标
- [ ] 18. 编写 pytest 解析器测试用例

## M3：P1 输出格式扩展

- [ ] 19. 实现 HTML 输出器：Markdown → HTML 转换
- [ ] 20. 实现 JSON 伴随产物输出
- [ ] 21. 配置项 `output_format` 完整支持 html / json

## M4：P2 后续迭代（不在本次任务范围）

- [ ] 22. 历史趋势对比：多次运行结果对比分析
- [ ] 23. 更多框架支持：Go test / cargo test
- [ ] 24. 报告在线托管 / Web 展示