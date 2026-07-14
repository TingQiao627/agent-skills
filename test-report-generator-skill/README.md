# 测试报告生成 Skill

自动解析测试结果并生成结构化标准测试报告。

## 快速开始

### 1. 解析已有测试结果

```javascript
const { parseJestJson } = require('./scripts/parsers/jest-parser');
const { generateMarkdownReport } = require('./scripts/report-generator');
const fs = require('fs');

// 读取 Jest JSON 结果
const jestResult = JSON.parse(fs.readFileSync('test-results.json', 'utf-8'));
const testData = parseJestJson(jestResult);

// 生成报告
const report = generateMarkdownReport(testData);
fs.writeFileSync('reports/test-report.md', report);
```

### 2. 自动检测框架并执行测试

```javascript
const { detectFramework } = require('./scripts/framework-detector');
const { execSync } = require('child_process');

// 检测框架
const detected = detectFramework();
if (detected) {
  console.log(`检测到框架: ${detected.framework}`);
  console.log(`执行命令: ${detected.command}`);
  
  // 执行测试
  const result = execSync(detected.command, { encoding: 'utf-8' });
  // ... 解析结果
}
```

## 支持的框架

| 框架 | 结果格式 | 状态 |
|------|---------|------|
| Jest | JSON reporter | ✅ P0 |
| Vitest | JSON reporter | ✅ P0 |
| pytest | JUnit XML | ✅ P1 |
| JUnit XML | XML | ✅ P0 (兜底) |

## 目录结构

```
test-report-generator-skill/
├── SKILL.md                    # Skill 定义文件
├── README.md                   # 使用说明
├── scripts/
│   ├── types.js                # 类型定义
│   ├── framework-detector.js   # 框架检测器
│   ├── parsers/
│   │   ├── jest-parser.js      # Jest/Vitest 解析器
│   │   └── junit-parser.js     # JUnit XML 解析器
│   └── report-generator.js     # 报告生成器
├── test-data/                  # 测试数据
└── validate.js                 # 验证脚本
```

## 验证

运行验证脚本：

```bash
cd test-report-generator-skill
node validate.js
```

## 报告格式

生成的报告包含以下章节：

1. **报告头** - 项目名、时间、命令、框架版本
2. **结果摘要** - 总数、通过率、耗时、结论标识
3. **失败用例分析** - 错误信息、堆栈、文件定位
4. **用例明细** - 按文件分组，超过200条截断
5. **覆盖率** - 语句/分支/函数/行覆盖率
6. **附录** - 原始文件路径、工具版本

## 验收标准

- ✅ 在 Jest/Vitest 项目中执行生成命令，产出符合结构的 Markdown 报告
- ✅ 存在失败用例时，报告包含用例名、文件路径、错误信息
- ✅ 提供 JUnit XML 文件可仅解析不执行测试
- ✅ 结果文件损坏时返回明确错误说明
- ✅ 覆盖率数据不存在时标注"未获取"