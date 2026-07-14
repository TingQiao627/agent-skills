# 测试报告 - unknown

**生成时间**: 2026/7/14 09:11:45
**执行命令**: `npm test`
**框架**: jest unknown

---

## 结果摘要

| 指标 | 数值 |
|------|------|
| 总用例数 | 5 |
| 通过 | 3 |
| 失败 | 1 |
| 跳过 | 1 |
| 通过率 | 60.0% |
| 耗时 | 430ms |

**结论**: ❌ 存在失败用例

## 失败用例分析

### 1. formatDate should handle invalid input

- **文件**: `utils/format.test.js`
- **错误**: Expected '2026-07-14' but received '2026-07-13'

## 用例明细

### utils/format.test.js

- ✅ formatDate should format date correctly (10ms)
- ❌ formatDate should handle invalid input (5ms)
- ✅ formatCurrency should format USD (8ms)
### utils/string.test.js

- ✅ capitalize should capitalize first letter (3ms)
- ⏭️ capitalize should handle empty string (0ms)

## 覆盖率

*未获取*

## 附录

- 生成工具: test-report-generator v1.0.0