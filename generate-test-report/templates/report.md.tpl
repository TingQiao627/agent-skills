# 测试报告模板

**生成时间**: {{timestamp}}  
**框架**: {{framework}} {{version}}  
**执行命令**: {{command}}

---

## 结果摘要

| 指标 | 值 |
|------|-----|
| 用例总数 | {{total}} |
| 通过 | {{passed}} |
| 失败 | {{failed}} |
| 跳过 | {{skipped}} |
| 通过率 | {{passRate}}% |
| 总耗时 | {{duration}}s |
| 结论 | {{status}} |

---

## 失败用例分析

{{#each failures}}
### ❌ {{testCaseName}}

**文件**: {{file}}  
**错误**: {{errorMessage}}

```
{{stackTrace}}
```

{{/each}}

---

## 用例明细

| 状态 | 用例名 | 文件 | 耗时 |
|------|--------|------|------|
{{#each testCases}}
| {{statusIcon}} | {{name}} | {{file}} | {{duration}}s |
{{/each}}

---

## 覆盖率

| 类型 | 覆盖率 |
|------|--------|
| 语句 | {{coverage.statements}}% |
| 分支 | {{coverage.branches}}% |
| 函数 | {{coverage.functions}}% |
| 行 | {{coverage.lines}}% |

---

## 附录

- **原始结果文件**: {{resultFile}}
- **生成工具**: generate-test-report v1.0.0