# Spec: test-report-generation

## FR1: 测试执行与结果收集

### FR1.1 框架自动识别
- 优先级：a. 用户显式命令 > b. 项目配置文件 > c. 框架特征文件推断
- 支持项目配置：package.json (scripts.test)、pyproject.toml、Cargo.toml
- 支持特征文件：jest.config.*、vitest.config.*、pytest.ini

### FR1.2 首期支持格式 (P0)
- JavaScript/TypeScript：Jest (JSON reporter)、Vitest (JSON reporter)
- Python：pytest (JUnit XML / JSON report)
- 通用：JUnit XML（跨语言兜底格式）

### FR1.3 双模式支持
- 执行模式：触发测试运行并收集结果
- 解析模式：跳过执行，直接解析已有结果文件

### FR1.4 错误处理
- 测试执行失败时给出明确诊断信息，不生成空报告

## FR2: 报告内容结构

固定顺序的章节：
1. 报告头：项目名、生成时间、执行命令、框架/版本、执行环境摘要
2. 结果摘要：用例总数、通过/失败/跳过数、通过率、总耗时、✅/❌ 结论
3. 失败用例分析：用例名、所属文件、错误信息、堆栈关键行
4. 用例明细：按测试文件分组的用例列表与耗时（>200 条截断并注明）
5. 覆盖率：语句/分支/函数/行覆盖率总表，低于阈值文件清单
6. 附录：原始结果文件路径、生成工具版本

## FR3: 输出格式与落盘

### FR3.1 输出格式
- 默认 Markdown (.md)
- P1：HTML、JSON（伴随产物）

### FR3.2 输出路径
- 默认：reports/test-report-<YYYYMMDD-HHmmss>.md
- 用户可指定路径

### FR3.3 输出反馈
- 报告路径 + 结果摘要 + 失败时附最关键的 1~3 条失败原因

## FR4: Skill 交互约定

### FR4.1 触发意图
- "生成测试报告"
- "跑一下测试并出报告"
- "把这个 junit.xml 转成测试报告"

### FR4.2 可配置项
| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| test_command | 自动检测 | 测试执行命令 |
| result_file | 自动检测 | 解析模式下的结果文件路径 |
| output_format | markdown | markdown / html / json |
| output_path | reports/ | 报告输出目录 |
| coverage | auto | auto / on / off |
| fail_threshold | 无 | 通过率低于该值时标记不达标 |

## NFR: 非功能需求

- NFR1：解析+报告生成（不含测试执行）应在 5 秒内完成（1000 用例规模）
- NFR2：格式异常时降级输出，不崩溃或静默丢数据
- NFR3：不泄露环境变量、密钥类内容
- NFR4：同一结果文件多次生成报告，内容一致（时间戳除外）
- NFR5：解析器采用插件式结构，新增框架不影响既有解析器