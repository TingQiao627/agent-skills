/**
 * 报告生成器
 */

const fs = require('fs');
const path = require('path');

/**
 * 生成 Markdown 报告
 */
function generateMarkdownReport(testResult, options = {}) {
  const lines = [];
  
  // 报告头
  lines.push(`# 测试报告 - ${testResult.project || '未知项目'}`);
  lines.push('');
  lines.push(`**生成时间**: ${formatTimestamp(testResult.timestamp)}`);
  lines.push(`**执行命令**: \`${testResult.command || '未知'}\``);
  lines.push(`**框架**: ${testResult.framework || '未知'} ${testResult.frameworkVersion || ''}`);
  lines.push('');
  lines.push('---');
  lines.push('');
  
  // 结果摘要
  lines.push('## 结果摘要');
  lines.push('');
  lines.push('| 指标 | 数值 |');
  lines.push('|------|------|');
  lines.push(`| 总用例数 | ${testResult.total || 0} |`);
  lines.push(`| 通过 | ${testResult.passed || 0} |`);
  lines.push(`| 失败 | ${testResult.failed || 0} |`);
  lines.push(`| 跳过 | ${testResult.skipped || 0} |`);
  lines.push(`| 通过率 | ${calculatePassRate(testResult)}% |`);
  lines.push(`| 耗时 | ${formatDuration(testResult.duration)} |`);
  lines.push('');
  
  const conclusion = testResult.failed > 0 ? '❌ 存在失败用例' : '✅ 全部通过';
  lines.push(`**结论**: ${conclusion}`);
  lines.push('');
  
  // 失败用例分析
  if (testResult.failed > 0) {
    lines.push('## 失败用例分析');
    lines.push('');
    
    let failureIndex = 1;
    for (const suite of testResult.suites || []) {
      for (const testCase of suite.cases || []) {
        if (testCase.status === 'failed') {
          lines.push(`### ${failureIndex}. ${testCase.name}`);
          lines.push('');
          lines.push(`- **文件**: \`${testCase.file}\``);
          lines.push(`- **错误**: ${testCase.error || '未知错误'}`);
          
          if (testCase.stack) {
            lines.push(`- **堆栈**:`);
            lines.push('```');
            lines.push(truncateStack(testCase.stack, 10));
            lines.push('```');
          }
          lines.push('');
          failureIndex++;
          
          if (failureIndex > 10) break; // 限制失败用例数量
        }
      }
      if (failureIndex > 10) break;
    }
  }
  
  // 用例明细
  lines.push('## 用例明细');
  lines.push('');
  
  let totalCases = 0;
  const maxCases = 200;
  
  for (const suite of testResult.suites || []) {
    lines.push(`### ${suite.name || suite.file}`);
    lines.push('');
    
    for (const testCase of suite.cases || []) {
      if (totalCases >= maxCases) {
        lines.push(`... 省略 ${testResult.total - maxCases} 条用例`);
        break;
      }
      
      const status = testCase.status === 'passed' ? '✅' : 
                     testCase.status === 'failed' ? '❌' : '⏭️';
      lines.push(`- ${status} ${testCase.name} (${formatDuration(testCase.duration)})`);
      totalCases++;
    }
    
    if (totalCases >= maxCases) break;
  }
  lines.push('');
  
  // 覆盖率
  lines.push('## 覆盖率');
  lines.push('');
  
  if (testResult.coverage) {
    lines.push('| 类型 | 覆盖率 |');
    lines.push('|------|--------|');
    lines.push(`| 语句 | ${testResult.coverage.statements || 0}% |`);
    lines.push(`| 分支 | ${testResult.coverage.branches || 0}% |`);
    lines.push(`| 函数 | ${testResult.coverage.functions || 0}% |`);
    lines.push(`| 行 | ${testResult.coverage.lines || 0}% |`);
    lines.push('');
    
    if (testResult.coverage.lowFiles && testResult.coverage.lowFiles.length > 0) {
      lines.push(`**低于阈值文件**: ${testResult.coverage.lowFiles.map(f => 
        `\`${f.file}\` (${f.coverage}%)`).join(', ')}`);
      lines.push('');
    }
  } else {
    lines.push('*未获取*');
    lines.push('');
  }
  
  // 附录
  lines.push('## 附录');
  lines.push('');
  if (testResult.resultFile) {
    lines.push(`- 原始结果: \`${testResult.resultFile}\``);
  }
  lines.push('- 生成工具: test-report-generator v1.0.0');
  
  return lines.join('\n');
}

/**
 * 生成 JSON 报告
 */
function generateJsonReport(testResult) {
  return JSON.stringify(testResult, null, 2);
}

/**
 * 生成报告
 */
function generateReport(testResult, options = {}) {
  const format = options.format || 'markdown';
  
  switch (format) {
    case 'markdown':
      return generateMarkdownReport(testResult, options);
    case 'json':
      return generateJsonReport(testResult);
    default:
      return generateMarkdownReport(testResult, options);
  }
}

/**
 * 保存报告到文件
 */
function saveReport(report, outputPath) {
  const dir = path.dirname(outputPath);
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }
  
  fs.writeFileSync(outputPath, report, 'utf-8');
  return outputPath;
}

// 工具函数
function formatTimestamp(timestamp) {
  return new Date(timestamp).toLocaleString('zh-CN', { hour12: false });
}

function formatDuration(ms) {
  if (!ms) return '0ms';
  if (ms < 1000) return `${ms}ms`;
  return `${(ms / 1000).toFixed(2)}s`;
}

function calculatePassRate(result) {
  if (!result.total || result.total === 0) return '0.0';
  return ((result.passed / result.total) * 100).toFixed(1);
}

function truncateStack(stack, maxLines) {
  if (!stack) return '';
  const lines = stack.split('\n');
  return lines.slice(0, maxLines).join('\n');
}

module.exports = {
  generateReport,
  generateMarkdownReport,
  generateJsonReport,
  saveReport
};