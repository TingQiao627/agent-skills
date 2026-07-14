#!/bin/bash
# JUnit XML 解析器 - 将标准 JUnit XML 转换为统一数据结构
# 用法: parse-junit.sh <junit-xml-file>

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

# 检查工具可用性
if command -v xq &> /dev/null; then
    PARSE_CMD="xq"
elif command -v yq &> /dev/null; then
    PARSE_CMD="yq -p=xml"
else
    error_exit "需要 xq 或 yq 工具进行 XML 解析"
fi

# 验证 XML 格式
if ! $PARSE_CMD '.' "$INPUT_FILE" &>/dev/null; then
    error_exit "XML 格式无效: $INPUT_FILE"
fi

# 解析 JUnit XML 并转换为统一结构
$PARSE_CMD '
{
  project: (.testsuites.testsuite.name // "junit-project"),
  framework: "junit",
  frameworkVersion: "",
  timestamp: (now | todate),
  command: "junit-xml-parser",
  environment: {
    os: (if env.OS then env.OS else "unknown" end),
    node: ""
  },
  summary: {
    total: (.testsuites.testsuite.tests // .testsuite.tests // 0 | tonumber),
    passed: 0,
    failed: ((.testsuites.testsuite.failures // .testsuite.failures // 0 | tonumber) + (.testsuites.testsuite.errors // .testsuite.errors // 0 | tonumber)),
    skipped: (.testsuites.testsuite.skipped // .testsuite.skipped // 0 | tonumber),
    pending: 0,
    duration: (.testsuites.testsuite.time // .testsuite.time // "0" | tonumber),
    successRate: 0
  },
  status: (if ((.testsuites.testsuite.failures // .testsuite.failures // "0" | tonumber) > 0) or ((.testsuites.testsuite.errors // .testsuite.errors // "0" | tonumber) > 0) then "failed" else "passed" end),
  testCases: [
    (.testsuites.testsuite.testcase // .testsuite.testcase // [])[] |
    {
      name: .@name,
      file: .@classname,
      status: (if .failure then "failed" elif .error then "failed" elif .skipped then "skipped" else "passed" end),
      duration: (.@time // "0" | tonumber),
      error: (if .failure then {
        message: (.failure.@message // ""),
        stack: (.failure.#text // ""),
        expected: "",
        actual: ""
      } elif .error then {
        message: (.error.@message // ""),
        stack: (.error.#text // ""),
        expected: "",
        actual: ""
      } else null end)
    }
  ],
  coverage: null,
  errors: []
}
' "$INPUT_FILE" 2>/dev/null | jq '
.summary.passed = (.summary.total - .summary.failed - .summary.skipped) |
.summary.successRate = (if .summary.total > 0 then ((.summary.passed / .summary.total) * 100 | floor) else 0 end)
'