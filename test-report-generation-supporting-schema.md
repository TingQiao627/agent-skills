# 统一数据结构 Schema

> 本文档定义测试报告生成 Skill 的内部数据结构，用于各解析器与报告生成器之间的数据流转。

---

## 1. 核心类型

### 1.1 TestResult（测试结果总览）

```json
{
  "project": "string (项目名)",
  "framework": "string (框架标识: jest | vitest | junit)",
  "frameworkVersion": "string (可选，框架版本)",
  "timestamp": "ISO8601 datetime (报告生成时间)",
  "command": "string (执行命令)",
  "environment": {
    "os": "string (操作系统)",
    "node": "string (Node 版本，可选)"
  },
  "summary": {
    "total": "integer (用例总数)",
    "passed": "integer (通过数)",
    "failed": "integer (失败数)",
    "skipped": "integer (跳过数)",
    "pending": "integer (待执行数，可选)",
    "duration": "float (总耗时，秒)",
    "successRate": "float (通过率，0-100)"
  },
  "status": "passed | failed | incomplete",
  "testCases": ["TestCase 数组"],
  "coverage": "Coverage 对象 (可选)",
  "errors": ["string 数组，解析过程中的错误或警告"]
}
```

### 1.2 TestCase（单个测试用例）

```json
{
  "id": "string (用例唯一标识，可选)",
  "name": "string (用例名称)",
  "file": "string (源文件路径)",
  "line": "integer (起始行号，可选)",
  "status": "passed | failed | skipped | pending",
  "duration": "float (耗时，秒)",
  "error": {
    "message": "string (错误信息)",
    "stack": "string (堆栈关键行，截断至可读长度)",
    "expected": "string (期望值，可选)",
    "actual": "string (实际值，可选)"
  },
  "retries": "integer (重试次数，可选)",
  "ancestorTitles": ["string 数组，describe 嵌套路径"]
}
```

### 1.3 Coverage（覆盖率数据）

```json
{
  "lines": {
    "total": "integer",
    "covered": "integer",
    "percentage": "float (0-100)"
  },
  "branches": {
    "total": "integer",
    "covered": "integer", 
    "percentage": "float"
  },
  "functions": {
    "total": "integer",
    "covered": "integer",
    "percentage": "float"
  },
  "statements": {
    "total": "integer",
    "covered": "integer",
    "percentage": "float"
  },
  "thresholdViolations": [
    {
      "file": "string (文件路径)",
      "type": "lines | branches | functions | statements",
      "actual": "float",
      "threshold": "float"
    }
  ]
}
```

---

## 2. 框架映射规则

### 2.1 Jest → 统一结构

| Jest 字段 | 统一字段 | 转换规则 |
|-----------|----------|----------|
| `success` | `status` | `success ? "passed" : "failed"` |
| `numTotalTests` | `summary.total` | 直接映射 |
| `numPassedTests` | `summary.passed` | 直接映射 |
| `numFailedTests` | `summary.failed` | 直接映射 |
| `numPendingTests` | `summary.pending` | 直接映射 |
| `testResults[].name` | `TestCase.file` | 去除项目前缀 |
| `testResults[].status` | `TestCase.status` | `passed` → `passed`, `failed` → `failed` |
| `testResults[].message` | `TestCase.error.message` | 合并第一条 failure message |
| `testResults[].assertionResults[]` | `testCases` | 扁平化为数组 |
| `coverageMap` | `coverage` | 转换为 Coverage 结构 |

### 2.2 Vitest → 统一结构

| Vitest 字段 | 统一字段 | 转换规则 |
|-------------|----------|----------|
| `testResults[].assertionResults[]` | `testCases` | 扁平化处理 |
| `success` | `status` | 根据是否有 failed 判断 |
| `duration` | `summary.duration` | 汇总所有用例耗时 |
| `ancestorTitles` | `ancestorTitles` | 保留 describe 嵌套路径 |

### 2.3 JUnit XML → 统一结构

| XML 元素/属性 | 统一字段 | 转换规则 |
|---------------|----------|----------|
| `<testsuite>` | 根对象 | 每个 testsuite 生成一个 TestResult |
| `tests` 属性 | `summary.total` | 直接映射 |
| `failures` 属性 | `summary.failed` | 直接映射 |
| `errors` 属性 | `summary.failed` | 累加到 failed |
| `skipped` 属性 | `summary.skipped` | 直接映射 |
| `time` 属性 | `summary.duration` | 字符串转 float |
| `<testcase>` | `TestCase` | 每个元素对应一个 TestCase |
| `<failure>` | `error` | 提取 message 属性和文本内容 |
| `<error>` | `error` | 同 failure 处理 |

---

## 3. 错误处理约定

### 3.1 解析错误

当输入文件格式异常时，输出结构须包含：

```json
{
  "status": "error",
  "errors": ["具体错误描述"],
  "summary": {
    "total": 0,
    "passed": 0,
    "failed": 0,
    "skipped": 0,
    "duration": 0
  }
}
```

### 3.2 字段缺失处理

- 必填字段缺失：记录到 `errors` 数组，使用默认值
- 可选字段缺失：省略该字段，不生成占位符

### 3.3 敏感信息过滤

以下内容须在堆栈中过滤：
- 环境变量值
- 密钥/令牌
- 用户家路径（替换为 `~`）