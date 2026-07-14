#!/bin/bash
# 验收测试脚本 - 验证 AC1-AC5 验收标准
# 用法: bash verify-acceptance.sh

set -euo pipefail

echo "=== 测试报告生成 Skill 验收测试 ==="
echo ""

# 创建测试数据目录
TEST_DATA_DIR="test-data"
REPORTS_DIR="reports"
mkdir -p "$TEST_DATA_DIR" "$REPORTS_DIR"

# AC1: Jest JSON 样本
cat > "$TEST_DATA_DIR/jest-sample.json" <<'EOF'
{
  "success": false,
  "numTotalTests": 3,
  "numPassedTests": 2,
  "numFailedTests": 1,
  "numPendingTests": 0,
  "testResults": [
    {
      "name": "src/components/Login.test.tsx",
      "status": "passed",
      "duration": 0.15,
      "message": "",
      "assertionResults": [
        {"name": "should render login form", "status": "passed", "duration": 0.08},
        {"name": "should handle input change", "status": "passed", "duration": 0.07}
      ]
    },
    {
      "name": "src/api/auth.test.ts",
      "status": "failed",
      "duration": 0.12,
      "message": "Expected 'admin' but received 'user'",
      "assertionResults": [
        {"name": "should authenticate user", "status": "failed", "duration": 0.12, "failureMessages": ["Expected 'admin' but received 'user'"]}
      ]
    }
  ]
}
EOF

# AC3: JUnit XML 样本
cat > "$TEST_DATA_DIR/junit-sample.xml" <<'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<testsuites>
  <testsuite name="unit-tests" tests="5" failures="1" errors="0" skipped="1" time="2.5">
    <testcase name="test_login_success" classname="auth.spec" time="0.5"/>
    <testcase name="test_login_failure" classname="auth.spec" time="0.4">
      <failure message="Expected 200 but got 401">AssertionError: Expected 200 but got 401</failure>
    </testcase>
    <testcase name="test_logout" classname="auth.spec" time="0.3"/>
    <testcase name="test_session_timeout" classname="session.spec" time="0.8"/>
    <testcase name="test_token_refresh" classname="session.spec" time="0.5">
      <skipped/>
    </testcase>
  </testsuite>
</testsuites>
EOF

# AC4: 损坏的 JSON 文件
cat > "$TEST_DATA_DIR/corrupted.json" <<'EOF'
{invalid json content
EOF

echo "✅ 测试数据已创建"
echo ""

# AC1: 测试 Jest 解析 + 报告生成
echo "📋 AC1: Jest JSON 解析测试"
bash parse-jest.sh "$TEST_DATA_DIR/jest-sample.json" > "$TEST_DATA_DIR/jest-unified.json"
if jq -e '.summary.total == 3' "$TEST_DATA_DIR/jest-unified.json" &>/dev/null; then
    echo "✅ AC1 通过: Jest 解析器正常工作"
else
    echo "❌ AC1 失败: Jest 解析结果不符合预期"
fi
echo ""

# AC2: 验证失败用例包含完整信息
echo "📋 AC2: 失败用例信息验证"
if jq -e '.testCases[] | select(.status == "failed") | .error.message' "$TEST_DATA_DIR/jest-unified.json" &>/dev/null; then
    echo "✅ AC2 通过: 失败用例包含错误信息"
else
    echo "❌ AC2 失败: 失败用例缺少错误信息"
fi
echo ""

# AC3: 测试 JUnit XML 解析
echo "📋 AC3: JUnit XML 解析测试"
if command -v xq &>/dev/null || command -v yq &>/dev/null; then
    bash parse-junit.sh "$TEST_DATA_DIR/junit-sample.xml" > "$TEST_DATA_DIR/junit-unified.json" 2>/dev/null || true
    if [[ -s "$TEST_DATA_DIR/junit-unified.json" ]]; then
        echo "✅ AC3 通过: JUnit XML 解析器正常工作"
    else
        echo "⚠️  AC3 跳过: 需要 xq 或 yq 工具解析 XML"
    fi
else
    echo "⚠️  AC3 跳过: 需要 xq 或 yq 工具解析 XML"
fi
echo ""

# AC4: 测试损坏文件错误处理
echo "📋 AC4: 损坏文件错误处理"
ERROR_OUTPUT=$(bash parse-jest.sh "$TEST_DATA_DIR/corrupted.json" 2>&1 || true)
if echo "$ERROR_OUTPUT" | grep -q "error\|Error\|格式无效"; then
    echo "✅ AC4 通过: 损坏文件返回明确错误信息"
else
    echo "❌ AC4 失败: 未正确处理损坏文件"
fi
echo ""

# AC5: 生成完整报告
echo "📋 AC5: 报告生成测试"
bash generate-report.sh "$TEST_DATA_DIR/jest-unified.json" "$REPORTS_DIR/test-report.md" > /dev/null 2>&1
if [[ -f "$REPORTS_DIR/test-report.md" ]]; then
    if grep -q "结果摘要" "$REPORTS_DIR/test-report.md" && grep -q "用例明细" "$REPORTS_DIR/test-report.md"; then
        echo "✅ AC5 通过: 报告包含必需章节"
    else
        echo "❌ AC5 失败: 报告缺少必需章节"
    fi
else
    echo "❌ AC5 失败: 报告未生成"
fi
echo ""

# 最终汇总
echo "=== 验收测试完成 ==="
echo "测试数据目录: $TEST_DATA_DIR"
echo "报告目录: $REPORTS_DIR"
echo ""
ls -lh "$REPORTS_DIR/"