#!/bin/bash
# Vitest JSON 解析器 - 将 Vitest --reporter=json 输出转换为统一数据结构
# 用法: parse-vitest.sh <vitest-json-file>

set -euo pipefail

INPUT_FILE="${1:-}"
OUTPUT_FORMAT="${OUTPUT_FORMAT:-json}"

# 错误处理函数
error_exit() {
    echo "{\"status\":\"error\",\"errors\":[\"$1\"],\"summary\":{\"total\":0,\"passed\":0,\"failed\":0,\"skipped\":0,\"duration\":0}}" >&2
    exit 1
}

# 参数校验
if [[ -z "$INPUT_FILE" ]]; then
    error_exit "缺少输入文件参数"
fi

if [[ ! -f "$INPUT_FILE" ]]; then
    error_exit "文件不存在: $INPUT_FILE"
fi

# 检查 jq 是否可用
if ! command -v jq &> /dev/null; then
    error_exit "需要 jq 工具进行 JSON 解析"
fi

# 验证 JSON 格式
if ! jq empty "$INPUT_FILE" 2>/dev/null; then
    error_exit "JSON 格式无效: $INPUT_FILE"
fi

# 解析 Vitest JSON 并转换为统一结构
jq '
{
  project: "vitest-project",
  framework: "vitest",
  frameworkVersion: "",
  timestamp: (now | todate),
  command: "vitest --run --reporter=json",
  environment: {
    os: (if env.OS then env.OS else "unknown" end),
    node: (if env.NODE_VERSION then env.NODE_VERSION else "" end)
  },
  summary: {
    total: (.testResults | length),
    passed: [.testResults[] | select(.status == "passed")] | length,
    failed: [.testResults[] | select(.status == "failed")] | length,
    skipped: [.testResults[] | select(.status == "skipped")] | length,
    pending: [.testResults[] | select(.status == "pending")] | length,
    duration: (.testResults | map(.duration) | add),
    successRate: 0
  },
  status: (if [.testResults[] | select(.status == "failed")] | length > 0 then "failed" else "passed" end),
  testCases: [
    .testResults[] |
    {
      name: .name,
      file: .location,
      status: .status,
      duration: .duration,
      error: (if .status == "failed" and (.errors | length) > 0 then {
        message: .errors[0].message,
        stack: (.errors[0].stack // ""),
        expected: (.errors[0].expected // ""),
        actual: (.errors[0].actual // "")
      } else null end),
      ancestorTitles: (.ancestorTitles // [])
    }
  ],
  coverage: null,
  errors: []
} |
.successRate as $sr | .summary.successRate = $sr
' "$INPUT_FILE" | jq '
.summary.successRate = (if .summary.total > 0 then ((.summary.passed / .summary.total) * 100 | floor) else 0 end)
'