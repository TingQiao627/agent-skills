# 测试报告模板

本文档定义测试报告的标准结构和内容格式。

## 报告结构

### 1. 报告头

```markdown
# 测试报告 - {项目名}

## 1. 报告头

| 项目 | 值 |
|------|-----|
| 项目名称 | {project_name} |
| 生成时间 | {timestamp} |
| 执行命令 | {command} |
| 框架/版本 | {framework} {version} |
| 执行环境 | {os} | Node {version} | Python {version} |
```

### 2. 结果摘要

```markdown
## 2. 结果摘要

**整体结论**: ✅ 全部通过 | ❌ 存在失败

| 指标 | 数值 |
|------|------|
| 用例总数 | {total} |
| 通过数 | {passed} ✅ |
| 失败数 | {failed} ❌ |
| 跳过数 | {skipped} |
| 通过率 | {pass_rate}% |
| 总耗时 | {duration} |
```

### 3. 失败用例分析

```markdown
## 3. 失败用例分析

### 失败用例 1: {test_name}

- **所属文件**: `{file_path}:{line}`
- **错误信息**:
  ```
  {error_message}
  ```
- **堆栈关键行**:
  ```
  {stack_trace_top_5_lines}
  ```

### 失败用例 2: ...
```

### 4. 用例明细

```markdown
## 4. 用例明细

### {test_file_1}

- ✅ {test_name_1} ({duration}ms)
- ❌ {test_name_2} ({duration}ms)
- ⏭️ {test_name_3} ({duration}ms)

> ℹ️ 共 {total} 条用例，本报告展示全部。超过 200 条时截断并注明。
```

### 5. 覆盖率

```markdown
## 5. 覆盖率

| 类型 | 覆盖率 | 状态 |
|------|--------|------|
| 语句 | {statements}% | ✅/⚠️ |
| 分支 | {branches}% | ✅/⚠️ |
| 函数 | {functions}% | ✅/⚠️ |
| 行 | {lines}% | ✅/⚠️ |

### ⚠️ 低于阈值文件

- `{file_path}`: {coverage}%（阈值：{threshold}%）
```

**降级处理**: 无覆盖率数据时标注「未获取」。

### 6. 附录

```markdown
## 6. 附录

| 项目 | 值 |
|------|-----|
| 原始结果文件 | `{result_file_path}` |
| 生成工具 | test-report-generation v1.0.0 |
| 报告路径 | `{output_path}` |
```

## 格式规范

### 状态图标

| 状态 | 图标 |
|------|------|
| 通过 | ✅ |
| 失败 | ❌ |
| 跳过 | ⏭️ |
| 警告 | ⚠️ |

### 覆盖率状态

| 范围 | 状态 |
|------|------|
| ≥80% | ✅ |
| 50-79% | ⚠️ |
| <50% | ❌ |

### 耗时格式

| 范围 | 格式 |
|------|------|
| <1s | `123ms` |
| 1s-60s | `5.23s` |
| >60s | `2m 30s` |

## 示例报告

参见 `test-report-generation/examples/sample-report.md`