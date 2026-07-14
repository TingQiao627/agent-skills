/**
 * Markdown 报告生成器
 * 
 * 根据 TestResult 结构生成标准 Markdown 格式测试报告
 */

const fs = require('fs');
const path = require('path');
const sanitizer = require('../utils/sanitizer');

/**
 * 生成 Markdown 测试报告
 * @param {TestResult} testResult - 标准测试结果对象
 * @param {Object} config - 配置项
 * @returns {string} 报告文件路径
 */
function generate(testResult, config = {}) {
  // 1. 敏感信息过滤
  const cleanedResult = sanitizer.sanitize(testResult);
  
  // 2. 构建报告内容
  const report = buildReport(cleanedResult, config);
  
  // 3. 确定输出路径
  const outputPath = config.output_path || 'reports';
  const timestamp = new Date().toISOString().replace(/[-:T]/g, '').split('.')[0];
  const filename = `test-report-${timestamp.substring(0, 15)}.md`;
  const reportPath = path.join(outputPath, filename);
  
  // 4. 确保输出目录存在
  const reportDir = path.dirname(reportPath);
  if (!fs.existsSync(reportDir)) {
    fs.mkdirSync(reportDir, { recursive: true });
  }
  
  // 5. 写入报告文件
  fs.writeFileSync(reportPath, report, 'utf-8');
  
  return reportPath;
}

/**
 * 构建完整报告内容
 */
function buildReport(result, config) {
  const sections = [];
  
  // 报告头
  sections.push(buildHeader(result, config));
  
  // 结果摘要
  sections.push(buildSummary(result));
  
  // 失败用例分析（有失败时）
  if (result.failed > 0) {
    sections.push(buildFailureAnalysis(result));
  }
  
  // 用例明细
  sections.push(buildTestDetails(result));
  
  // 覆盖率（若可获取）
  sections.push(buildCoverage(result));
  
  // 附录
  sections.push(buildAppendix(result, config));
  
  return sections.join('\n\n');
}

/**
 * 构建报告头
 */
function buildHeader(result, config) {
  const projectName = config.project_name || '未命名项目';
  const timestamp = new Date().toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai' });
  const command = config.test_command || '自动检测';
  const env = getEnvironmentInfo();
  
  return `# 测试报告 - ${projectName}

## 1. 报告头

| 项目 | 值 |
|------|-----|
| 项目名称 | ${projectName} |
| 生成时间 | ${timestamp} |
| 执行命令 | \`${command}\` |
| 框架/版本 | ${result.framework} ${result.version} |
| 执行环境 | ${env} |`;
}

/**
 * 构建结果摘要
 */
function buildSummary(result) {
  const statusEmoji = result.failed === 0 ? '✅' : '❌';
  const statusText = result.failed === 0 ? '全部通过' : '存在失败';
  
  return `## 2. 结果摘要

**整体结论**: ${statusEmoji} ${statusText}

| 指标 | 数值 |
|------|------|
| 用例总数 | ${result.total} |
| 通过数 | ${result.passed} ✅ |
| 失败数 | ${result.failed} ${result.failed > 0 ? '❌' : ''} |
| 跳过数 | ${result.skipped} |
| 通过率 | ${result.passRate}% |
| 总耗时 | ${formatDuration(result.duration)} |`;
}

/**
 * 构建失败用例分析
 */
function buildFailureAnalysis(result) {
  let content = '## 3. 失败用例分析\n\n';
  
  let failureIndex = 0;
  result.testFiles.forEach(file => {
    file.tests.forEach(test => {
      if (test.status === 'failed') {
        failureIndex++;
        content += `### 失败用例 ${failureIndex}: ${test.name}\n\n`;
        content += `- **所属文件**: \`${file.path}\`\n\n`;
        
        if (test.error) {
          content += `- **错误信息**:\n`;
          content += '```\n';
          content += test.error.message;
          content += '\n```\n\n';
          
          if (test.error.stack && test.error.stack.length > 0) {
            content += `- **堆栈关键行**:\n`;
            content += '```\n';
            content += test.error.stack.join('\n');
            content += '\n```\n\n';
          }
        }
      }
    });
  });
  
  return content;
}

/**
 * 构建用例明细
 */
function buildTestDetails(result) {
  let content = '## 4. 用例明细\n\n';
  
  let totalShown = 0;
  const maxTests = 200;
  const isTruncated = result.total > maxTests;
  
  result.testFiles.forEach(file => {
    content += `### ${file.path}\n\n`;
    
    file.tests.forEach(test => {
      totalShown++;
      if (totalShown <= maxTests || !isTruncated) {
        const statusIcon = getStatusIcon(test.status);
        const duration = test.duration ? ` (${test.duration}ms)` : '';
        content += `- ${statusIcon} ${test.name}${duration}\n`;
      }
    });
    
    content += '\n';
  });
  
  if (isTruncated) {
    content += `\n> ℹ️ 共 ${result.total} 条用例，本报告展示前 ${maxTests} 条。完整明细请查看原始结果文件。\n`;
  } else {
    content += `\n> ℹ️ 共 ${result.total} 条用例，本报告展示全部。\n`;
  }
  
  return content;
}

/**
 * 构建覆盖率章节
 */
function buildCoverage(result) {
  let content = '## 5. 覆盖率\n\n';
  
  if (!result.coverage) {
    content += '> 未获取覆盖率数据\n';
    return content;
  }
  
  const cov = result.coverage;
  
  content += '| 类型 | 覆盖率 | 状态 |\n';
  content += '|------|--------|------|\n';
  content += `| 语句 | ${cov.statements}% | ${getStatusEmoji(cov.statements)} |\n`;
  content += `| 分支 | ${cov.branches}% | ${getStatusEmoji(cov.branches)} |\n`;
  content += `| 函数 | ${cov.functions}% | ${getStatusEmoji(cov.functions)} |\n`;
  content += `| 行 | ${cov.lines}% | ${getStatusEmoji(cov.lines)} |\n\n`;
  
  if (cov.lowCoverageFiles && cov.lowCoverageFiles.length > 0) {
    content += '### ⚠️ 低于阈值文件\n\n';
    cov.lowCoverageFiles.forEach(file => {
      content += `- \`${file.path}\`: ${file.coverage}%\n`;
    });
  }
  
  return content;
}

/**
 * 构建附录
 */
function buildAppendix(result, config) {
  const resultFile = config.result_file || '自动生成';
  const toolVersion = 'test-report-generation v1.0.0';
  
  return `## 6. 附录

| 项目 | 值 |
|------|-----|
| 原始结果文件 | \`${resultFile}\` |
| 生成工具 | ${toolVersion} |
| 报告路径 | \`reports/test-report-<timestamp>.md\` |`;
}

/**
 * 获取环境信息
 */
function getEnvironmentInfo() {
  const os = require('os');
  const platform = os.platform();
  const arch = os.arch();
  
  let nodeVersion = '';
  let pythonVersion = '';
  
  try {
    nodeVersion = process.version || '';
  } catch (e) {}
  
  return `${platform} ${arch} | Node ${nodeVersion}`;
}

/**
 * 格式化耗时
 */
function formatDuration(ms) {
  if (ms < 1000) {
    return `${ms}ms`;
  } else if (ms < 60000) {
    return `${(ms / 1000).toFixed(2)}s`;
  } else {
    const minutes = Math.floor(ms / 60000);
    const seconds = Math.floor((ms % 60000) / 1000);
    return `${minutes}m ${seconds}s`;
  }
}

/**
 * 获取状态图标
 */
function getStatusIcon(status) {
  const icons = {
    'passed': '✅',
    'failed': '❌',
    'skipped': '⏭️'
  };
  return icons[status] || '❓';
}

/**
 * 获取覆盖率状态表情
 */
function getStatusEmoji(percentage) {
  if (percentage >= 80) return '✅';
  if (percentage >= 50) return '⚠️';
  return '❌';
}

module.exports = {
  generate,
  buildReport
};