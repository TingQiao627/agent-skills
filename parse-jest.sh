#!/bin/bash
# Jest JSON 解析器 - 将 Jest --json 输出转换为统一数据结构
# 用法: parse-jest.sh <jest-json-file>

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

# 解析 Jest JSON 并转换为统一结构
jq '
{
  project: (.testResults[0].name | gsub("^[^/]+/"; "") | split("/")[0]) // "unknown-project",
  framework: "jest",
  frameworkVersion: "",  # Jest 不在 JSON 输出中包含版本
  timestamp: (now | todate),
  command: "jest --json",
  environment: {
    os: (if env.OS then env.OS else "unknown" end),
    node: (if env.NODE_VERSION then env.NODE_VERSION else "" end)
  },
  summary: {
    total: .numTotalTests,
    passed: .numPassedTests,
    failed: .numFailedTests,
    skipped: .numPendingTests,
    pending: .numPendingTests,
    duration: (.testResults | map(.duration) | add),
    successRate: (if .numTotalTests > 0 then ((.numPassedTests / .numTotalTests) * 100 | floor) else 0 end)
  },
  status: (if .success then "passed" else "failed" end),
  testCases: [
    .testResults[] |
    {
      name: .name,
      file: .name,
      status: (if .status == "passed" then "passed" elif .status == "failed" then "failed" else "skipped" end),
      duration: .duration,
      error: (if .status == "failed" and (.message | length) > 0 then {
        message: .message,
        stack: (.assertionResults[0].failureMessages[0] // ""),
        expected: "",
        actual: ""
      } else null end)
    }
  ],
  coverage: null,  # Jest 覆盖率需要单独处理
  errors: []
}
' "$INPUT_FILE"