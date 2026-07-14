#!/bin/bash
# 报告生成器 - 读取统一数据结构生成 Markdown 测试报告
# 用法: generate-report.sh <unified-json-file> [output-path]

set -euo pipefail

INPUT_FILE="${1:-}"
OUTPUT_PATH="${2:-reports/test-report-$(date +%Y%m%d-%H%M%S).md}"

# 错误处理函数
error_exit() {
    echo "错误: $1" >&2
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

# 创建输出目录
mkdir -p "$(dirname "$OUTPUT_PATH")"

# 生成 Markdown 报告
{
    # 报告头
    jq -r '
        "# 测试报告\n",
        "\n> 项目: \(.project)",
        "\n> 生成时间: \(.timestamp)",
        "\n> 框架: \(.framework) \(.frameworkVersion)",
        "\n> 执行命令: \(.command)",
        "\n> 执行环境: \(.environment.os)\(if .environment.node != "" then " / Node " + .environment.node else "" end)",
        "\n"
    ' "$INPUT_FILE"
    
    # 结果摘要
    jq -r '
        "\n## 结果摘要\n",
        "\n| 指标 | 数值 |",
        "\n|------|------|",
        "\n| 用例总数 | \(.summary.total) |",
        "\n| 通过 | \(.summary.passed) |",
        "\n| 失败 | \(.summary.failed) |",
        "\n| 跳过 | \(.summary.skipped) |",
        "\n| 通过率 | \(.summary.successRate)% |",
        "\n| 总耗时 | \(.summary.duration)s |",
        "\n| 整体结论 | \(if .status == "passed" then "✅ 通过" else "❌ 失败" end) |",
        "\n"
    ' "$INPUT_FILE"
    
    # 失败用例分析
    if jq -e '.testCases[] | select(.status == "failed")' "$INPUT_FILE" &>/dev/null; then
        jq -r '
            "\n## 失败用例分析\n",
            (.testCases[] | select(.status == "failed") |
                "\n### \(.name)",
                "\n- **文件**: \(.file)",
                "\n- **错误信息**: \(.error.message)",
                (if .error.stack != "" then "\n- **堆栈**:\n```\n\(.error.stack)\n```\n" else "" end)
            )
        ' "$INPUT_FILE"
    fi
    
    # 用例明细
    echo -e "\n## 用例明细\n"
    jq -r '
        (.testCases | group_by(.file) | .[] |
            "\n### \((.[0].file))\n\n",
            "| 用例名 | 状态 | 耗时 |",
            "\n|--------|------|------|",
            (.[] | "| \(.name) | \(if .status == "passed" then "✅ 通过" elif .status == "failed" then "❌ 失败" else "⏭️ 跳过" end) | \(.duration)s |")
        )
    ' "$INPUT_FILE"
    
    # 覆盖率
    if jq -e '.coverage != null' "$INPUT_FILE" &>/dev/null; then
        jq -r '
            "\n## 覆盖率\n",
            "\n| 类型 | 覆盖率 |",
            "\n|------|--------|",
            "\n| 语句 | \(.coverage.statements.percentage)% |",
            "\n| 分支 | \(.coverage.branches.percentage)% |",
            "\n| 函数 | \(.coverage.functions.percentage)% |",
            "\n| 行 | \(.coverage.lines.percentage)% |",
            "\n"
        ' "$INPUT_FILE"
    else
        echo -e "\n## 覆盖率\n\n> 未获取覆盖率数据\n"
    fi
    
    # 附录
    jq -r '
        "\n## 附录\n",
        "\n- **原始结果文件**: \($INPUT_FILE)",
        "\n- **生成工具**: test-report-generation Skill v1.0.0",
        "\n"
    ' --arg INPUT_FILE "$INPUT_FILE" "$INPUT_FILE"
    
} > "$OUTPUT_PATH"

echo "✅ 报告已生成: $OUTPUT_PATH"
echo ""
jq -r '
    "摘要: 通过率 \(.summary.successRate)%, 失败 \(.summary.failed) 条",
    (if .summary.failed > 0 then
        "\n失败原因摘要:",
        (.testCases[] | select(.status == "failed") | "- \(.name): \(.error.message)") | .[0:3]
    else "" end)
' "$INPUT_FILE"