/**
 * Markdown 报告生成器
 */

import * as path from 'path';
import * as fs from 'fs';
import { ParsedTestResult } from './parsers/base-parser';

export interface ReportOptions {
  outputPath?: string;
  failThreshold?: number;
  maxTestCases?: number; // 默认 200
}

export function generateMarkdownReport(
  result: ParsedTestResult,
  options: ReportOptions = {}
): string {
  const lines: string[] = [];
  const maxTestCases = options.maxTestCases || 200;
  
  // 1. 报告头
  lines.push('# 测试报告');
  lines.push('');
  lines.push(`> **项目**: ${path.basename(process.cwd())}`);
  lines.push(`> **生成时间**: ${new Date().toISOString().replace('T', ' ').slice(0, 19)}`);
  lines.push(`> **框架**: ${result.framework}${result.version ? ` v${result.version}` : ''}`);
  if (result.metadata.command) {
    lines.push(`> **执行命令**: \`${result.metadata.command}\``);
  }
  if (result.metadata.resultFile) {
    lines.push(`> **结果文件**: ${result.metadata.resultFile}`);
  }
  lines.push('');
  
  // 2. 结果摘要
  lines.push('## 📊 结果摘要');
  lines.push('');
  
  const icon = result.summary.success ? '✅' : '❌';
  const thresholdNote = options.failThreshold !== undefined && result.summary.passRate < options.failThreshold
    ? ` ⚠️ 低于阈值 ${options.failThreshold}%`
    : '';
  
  lines.push(`**整体结论**: ${icon} ${result.summary.success ? '通过' : '失败'}${thresholdNote}`);
  lines.push('');
  lines.push('| 指标 | 数值 |');
  lines.push('|------|------|');
  lines.push(`| 用例总数 | ${result.summary.total} |`);
  lines.push(`| 通过 | ${result.summary.passed} |`);
  lines.push(`| 失败 | ${result.summary.failed} |`);
  lines.push(`| 跳过 | ${result.summary.skipped} |`);
  lines.push(`| 通过率 | ${result.summary.passRate}% |`);
  lines.push(`| 总耗时 | ${formatDuration(result.summary.duration)} |`);
  lines.push('');
  
  // 3. 失败用例分析
  if (result.summary.failed > 0) {
    lines.push('## ❌ 失败用例分析');
    lines.push('');
    
    let count = 0;
    for (const file of result.files) {
      const failedCases = file.testCases.filter(tc => tc.status === 'failed');
      for (const tc of failedCases) {
        count++;
        lines.push(`### ${count}. ${tc.name}`);
        lines.push('');
        lines.push(`- **文件**: ${tc.file}`);
        if (tc.error) {
          lines.push(`- **错误信息**: ${tc.error.message}`);
          if (tc.error.stack) {
            lines.push('');
            lines.push('```');
            lines.push(tc.error.stack);
            lines.push('```');
          }
        }
        lines.push('');
      }
    }
  }
  
  // 4. 用例明细
  lines.push('## 📝 用例明细');
  lines.push('');
  
  let totalCases = 0;
  const truncatedFiles: string[] = [];
  
  for (const file of result.files) {
    if (totalCases >= maxTestCases) {
      truncatedFiles.push(file.file);
      continue;
    }
    
    const remaining = maxTestCases - totalCases;
    const cases = file.testCases.slice(0, remaining);
    totalCases += cases.length;
    
    if (file.testCases.length > cases.length) {
      truncatedFiles.push(file.file);
    }
    
    lines.push(`### ${file.file}`);
    lines.push('');
    lines.push('| 用例 | 状态 | 耗时 |');
    lines.push('|------|------|------|');
    
    for (const tc of cases) {
      const statusIcon = tc.status === 'passed' ? '✅' : tc.status === 'failed' ? '❌' : '⏭️';
      lines.push(`| ${tc.name} | ${statusIcon} ${tc.status} | ${tc.duration}ms |`);
    }
    lines.push('');
  }
  
  if (truncatedFiles.length > 0 || totalCases >= maxTestCases) {
    lines.push(`> ⚠️ 用例明细已截断（超过 ${maxTestCases} 条）`);
    lines.push('');
  }
  
  // 5. 覆盖率
  lines.push('## 📈 覆盖率');
  lines.push('');
  
  if (result.coverage) {
    lines.push('| 类型 | 覆盖 | 总计 | 百分比 |');
    lines.push('|------|------|------|--------|');
    lines.push(`| 语句 | ${result.coverage.statements.covered} | ${result.coverage.statements.total} | ${result.coverage.statements.percentage}% |`);
    lines.push(`| 分支 | ${result.coverage.branches.covered} | ${result.coverage.branches.total} | ${result.coverage.branches.percentage}% |`);
    lines.push(`| 函数 | ${result.coverage.functions.covered} | ${result.coverage.functions.total} | ${result.coverage.functions.percentage}% |`);
    lines.push(`| 行 | ${result.coverage.lines.covered} | ${result.coverage.lines.total} | ${result.coverage.lines.percentage}% |`);
    lines.push('');
  } else {
    lines.push('> 未获取');
    lines.push('');
  }
  
  // 6. 附录
  lines.push('## 📎 附录');
  lines.push('');
  lines.push('- **生成工具**: test-report-generation v1.0.0');
  if (result.metadata.resultFile) {
    lines.push(`- **原始结果文件**: ${result.metadata.resultFile}`);
  }
  lines.push('');
  
  return lines.join('\n');
}

/**
 * 保存报告到文件
 */
export function saveReport(
  content: string,
  options: ReportOptions = {}
): string {
  const outputDir = options.outputPath || 'reports';
  
  if (!fs.existsSync(outputDir)) {
    fs.mkdirSync(outputDir, { recursive: true });
  }
  
  const timestamp = new Date().toISOString().replace(/[:-]/g, '').replace('T', '-').slice(0, 15);
  const filename = `test-report-${timestamp}.md`;
  const filepath = path.join(outputDir, filename);
  
  fs.writeFileSync(filepath, content, 'utf-8');
  
  return filepath;
}

function formatDuration(ms: number): string {
  if (ms < 1000) return `${ms}ms`;
  if (ms < 60000) return `${(ms / 1000).toFixed(2)}s`;
  return `${Math.floor(ms / 60000)}m ${Math.floor((ms % 60000) / 1000)}s`;
}