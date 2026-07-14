/**
 * Markdown 报告生成器
 * 
 * 生成标准 Markdown 格式测试报告
 */

import { ParsedTestResult } from '../types';
import { sanitizeReportContent } from '../security-filter';

export function generateMarkdownReport(result: ParsedTestResult): string {
  const sections = [
    generateHeader(result),
    generateSummary(result.summary),
    generateFailures(result.failures),
    generateTestCases(result.testCases),
    generateCoverage(result.coverage),
    generateAppendix(result.metadata),
  ];
  
  return sections.filter(s => s).join('\n\n');
}

function generateHeader(result: ParsedTestResult): string {
  const timestamp = new Date(result.metadata.timestamp).toLocaleString('zh-CN');
  
  return `# 测试报告

**生成时间**: ${timestamp}  
**框架**: ${result.metadata.framework}${result.metadata.version ? ` ${result.metadata.version}` : ''}  
${result.metadata.command ? `**执行命令**: \`${result.metadata.command}\`` : ''}`;
}

function generateSummary(summary: any): string {
  const status = summary.failed === 0 ? '✅ PASS' : '❌ FAIL';
  
  return `## 结果摘要

| 指标 | 值 |
|------|-----|
| 用例总数 | ${summary.total} |
| 通过 | ${summary.passed} |
| 失败 | ${summary.failed} |
| 跳过 | ${summary.skipped} |
| 通过率 | ${summary.passRate.toFixed(1)}% |
| 总耗时 | ${(summary.duration / 1000).toFixed(2)}s |
| 结论 | ${status} |`;
}

function generateFailures(failures: any[]): string {
  if (failures.length === 0) return '';
  
  const items = failures.map(f => `
### ❌ ${sanitizeReportContent(f.testCaseName)}

**文件**: ${f.file}  
**错误**: ${sanitizeReportContent(f.errorMessage)}

\`\`\`
${f.stackTrace}
\`\`\`
`).join('\n');
  
  return `## 失败用例分析\n\n${items}`;
}

function generateTestCases(testCases: any[]): string {
  if (testCases.length === 0) return '';
  
  // 超过 200 条截断
  const display = testCases.slice(0, 200);
  const truncated = testCases.length > 200;
  
  const rows = display.map(tc => 
    `| ${tc.status === 'passed' ? '✅' : tc.status === 'failed' ? '❌' : '⏭️'} | ${sanitizeReportContent(tc.name)} | ${tc.file} | ${(tc.duration / 1000).toFixed(2)}s |`
  ).join('\n');
  
  return `## 用例明细

| 状态 | 用例名 | 文件 | 耗时 |
|------|--------|------|------|
${rows}

${truncated ? `> 共 ${testCases.length} 条用例，仅展示前 200 条` : ''}`;
}

function generateCoverage(coverage: any): string {
  if (!coverage) return '## 覆盖率\n\n**未获取**';
  
  return `## 覆盖率

| 类型 | 覆盖率 |
|------|--------|
| 语句 | ${coverage.statements.toFixed(1)}% |
| 分支 | ${coverage.branches.toFixed(1)}% |
| 函数 | ${coverage.functions.toFixed(1)}% |
| 行 | ${coverage.lines.toFixed(1)}% |

${coverage.lowCoverageFiles?.length ? `**低覆盖率文件**: ${coverage.lowCoverageFiles.join(', ')}` : ''}`;
}

function generateAppendix(metadata: any): string {
  return `## 附录

- **原始结果文件**: 自动检测
- **生成工具**: generate-test-report v1.0.0`;
}