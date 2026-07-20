# Design: test-report-generation

## Architecture

```
skills/test-report-generation/
├── SKILL.md              # Skill 定义文件
└── scripts/
    ├── parse_results.py  # 结果解析器（插件式）
    └── generate_report.py # 报告生成器
```

## Design Decisions

### D1: 解析器采用插件式结构
- 每个框架/格式对应一个解析器类，实现统一接口 `parse(raw_data) -> ParsedResult`
- 新增框架支持只需添加新解析器，不影响既有解析器

### D2: 双模式设计
- 执行模式：Skill 通过 `exec` 运行测试命令，收集 stdout/stderr 和产物文件
- 解析模式：跳过执行，直接读取用户指定的结果文件

### D3: 报告模板化
- Markdown 报告使用 Jinja2 模板生成
- 报告结构固定：报告头 → 摘要 → 失败分析 → 用例明细 → 覆盖率 → 附录

### D4: 覆盖率数据来源
- Jest/Vitest：`coverage/coverage-summary.json` 或 `coverage/coverage-final.json`
- pytest：`coverage.xml` (pytest-cov)
- 通用：`cobertura.xml`

### D5: 配置项
- test_command：自动检测 > 用户显式指定
- result_file：解析模式下的结果文件路径
- output_format：markdown（默认）/ html / json
- output_path：reports/（默认）
- coverage：auto（默认）/ on / off
- fail_threshold：通过率阈值

## Data Flow

```
[测试命令] or [结果文件]
       ↓
  结果收集 (exec + 文件读取)
       ↓
  格式识别 (JUnit XML / Jest JSON / Vitest JSON / pytest)
       ↓
  解析器 → ParsedResult (统一数据结构)
       ↓
  报告生成器 → Markdown / HTML / JSON
       ↓
  落盘 reports/test-report-<timestamp>.md
```

## ParsedResult 数据结构

```python
{
  "project_name": str,
  "framework": str,
  "command": str,
  "timestamp": str,
  "summary": {
    "total": int,
    "passed": int,
    "failed": int,
    "skipped": int,
    "pass_rate": float,
    "duration": float,
    "conclusion": "pass" | "fail"
  },
  "failures": [{
    "name": str,
    "file": str,
    "error": str,
    "stack": str
  }],
  "test_cases": [{
    "name": str,
    "file": str,
    "status": str,
    "duration": float
  }],
  "coverage": {
    "statements": float,
    "branches": float,
    "functions": float,
    "lines": float,
    "low_files": [{"file": str, "rate": float}]
  },
  "artifacts": [str]
}
```