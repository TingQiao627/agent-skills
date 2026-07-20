# Tasks: test-report-generator

## Milestone 1: P0 — Jest/Vitest + JUnit XML 解析 + Markdown 报告 + 双模式

### Phase 1.1: 基础架构搭建
- [ ] **T1.1.1** 创建 Skill 目录结构 `skills/test-report-generator/`，含 `SKILL.md`、`scripts/`
- [ ] **T1.1.2** 实现配置解析模块 `scripts/config.py`：解析用户配置、默认值、框架自动检测
- [ ] **T1.1.3** 定义数据模型 `scripts/models.py`：`TestResult`、`TestSummary`、`TestSuite`、`TestCase`、`CoverageData`
- [ ] **T1.1.4** 实现解析器基类 `scripts/parsers/base.py` 与注册表 `scripts/parsers/__init__.py`

### Phase 1.2: 解析器实现
- [ ] **T1.2.1** 实现 Jest JSON 解析器 `scripts/parsers/jest_parser.py`
- [ ] **T1.2.2** 实现 Vitest JSON 解析器 `scripts/parsers/vitest_parser.py`
- [ ] **T1.2.3** 实现通用 JUnit XML 解析器 `scripts/parsers/junit_parser.py`
- [ ] **T1.2.4** 实现解析器自动选择逻辑（根据文件扩展名和内容特征）

### Phase 1.3: 报告生成
- [ ] **T1.3.1** 实现 Markdown 报告生成器 `scripts/reporter.py`（含完整模板）
- [ ] **T1.3.2** 实现报告头、摘要、失败分析、用例明细、附录章节渲染
- [ ] **T1.3.3** 实现用例截断逻辑（超过 200 条时截断并注明）
- [ ] **T1.3.4** 实现输出路径管理（默认 `reports/test-report-<timestamp>.md`）

### Phase 1.4: 执行与解析双模式
- [ ] **T1.4.1** 实现测试执行器 `scripts/runner.py`（执行模式 + 后台任务支持）
- [ ] **T1.4.2** 实现解析模式入口（跳过执行，直接读取结果文件）
- [ ] **T1.4.3** 实现测试执行失败诊断（命令不存在、框架未识别等）

### Phase 1.5: Skill 入口与集成
- [ ] **T1.5.1** 编写 `SKILL.md`：触发条件、工作流程、配置项说明、使用示例
- [ ] **T1.5.2** 实现主入口脚本 `scripts/main.py`：编排 config → collect → parse → report 流程
- [ ] **T1.5.3** 端到端集成测试：Jest 项目执行模式
- [ ] **T1.5.4** 端到端集成测试：Vitest 项目执行模式
- [ ] **T1.5.5** 端到端集成测试：JUnit XML 解析模式

### Phase 1.6: 健壮性与边界
- [ ] **T1.6.1** 实现降级输出：字段缺失标注"未获取"
- [ ] **T1.6.2** 实现结果文件损坏时的错误说明
- [ ] **T1.6.3** 实现报告安全过滤：排除环境变量、密钥类内容
- [ ] **T1.6.4** 验证幂等性：同一结果文件多次生成报告内容一致

---

## Milestone 2: P1 — pytest 支持 + 覆盖率 + fail_threshold

- [ ] **T2.1** 实现 pytest 解析器 `scripts/parsers/pytest_parser.py`（JUnit XML + JSON report）
- [ ] **T2.2** 实现覆盖率收集器 `scripts/coverage.py`（auto/on/off 三种模式）
- [ ] **T2.3** 实现覆盖率章节渲染到 Markdown 报告
- [ ] **T2.4** 实现 `fail_threshold` 逻辑：通过率低于阈值时结论标记为不达标
- [ ] **T2.5** 端到端测试：pytest 项目执行模式

---

## Milestone 3: P1 — HTML 输出 + JSON 伴随产物

- [ ] **T3.1** 实现 HTML 报告生成器（基于 Jinja2 模板）
- [ ] **T3.2** 实现 JSON 结构化数据输出（伴随产物）
- [ ] **T3.3** 端到端测试：HTML 格式输出验证

---

## Milestone 4: P2 — 后续迭代（本期不做）

- [ ] 历史趋势对比
- [ ] Go test / cargo test 支持
- [ ] 更多覆盖率工具集成