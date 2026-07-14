/**
 * Markdown 报告生成器
 */

import { TestResult, FailureCase, TestCaseGroup, CoverageData } from '../parser/base';

export class MarkdownGenerator {
  /**
   * 生成 Markdown 格式的测试报告
   */
  generate(result: TestResult): string {
    const sections = [
      this.generateHeader(result),
      this.generateSummary(result),
      this.generateFailures(result),
      this.generateTestCases(result),
      this.generateCoverage(result),
      this.generateAppendix(result)
    ];
    
    return sections.filter(s => s.length > 0).join('\n\n---\n\n');
  }
  
  /**
   * 生成报告头
   */
  private generateHeader(result: TestResult): string {
    const { metadata } = result;
    
    return `# 测试报告 - ${metadata.projectName}

> 生成时间: ${metadata.generatedAt}  
> 测试框架: ${metadata.framework}${metadata.frameworkVersion ? ` v${metadata.frameworkVersion}` : ''}  
> 执行命令: \`${metadata.testCommand}\`  
> 执行环境: ${metadata.environment}`;
  }
  
  /**
   * 生成结果摘要
   */
  private generateSummary(result: TestResult): string {
    const { summary } = result;
    const statusIcon = summary.status === 'passed' ? '✅' : '❌';
    const statusText = summary.status === 'passed' ? '通过' : '未通过';
    
    return `## 📊 结果摘要

| 指标 | 数值 |
|------|------|
| 用例总数 | ${summary.total} |
| ✅ 通过 | ${summary.passed} |
| ❌ 失败 | ${summary.failed} |
| ⏭️ 跳过 | ${summary.skipped} |
| 通过率 | ${summary.passRate}% |
| 总耗时 | ${(summary.duration / 1000).toFixed(2)}s |

**整体结论**: ${statusIcon} ${statusText}`;
  }
  
  /**
   * 生成失败用例分析
   */
  private generateFailures(result: TestResult): string {
    if (result.failures.length === 0) {
      return '';
    }
    
    const failureSections = result.failures.map((failure, index) => {
      return `### ${index + 1}. ${failure.name}

- **文件**: \`${failure.file}\`
- **错误信息**:
  \`\`\`
  ${this.truncateMessage(failure.error.message, 200)}
  \`\`\`${failure.error.stack ? `
- **堆栈摘要**:
  \`\`\`
  ${failure.error.stack}
  \`\`\`` : ''}${failure.duration ? `
- **耗时**: ${failure.duration}ms` : ''}`;
    });
    
    return `## ❌ 失败用例分析

${failureSections.join('\n\n')}`;
  }
  
  /**
   * 生成用例明细
   */
  private generateTestCases(result: TestResult): string {
    const { testCases } = result;
    
    // 检查是否需要截断
    const totalCases = testCases.reduce((sum, group) => sum + group.cases.length, 0);
    const needsTruncation = totalCases > 200;
    
    const caseSections = testCases.map((group, groupIndex) => {
      const rows = group.cases.map(c => {
        const statusIcon = c.status === 'passed' ? '✅' : c.status === 'failed' ? '❌' : '⏭️';
        return `| ${c.name} | ${statusIcon} | ${c.duration}ms |`;
      }).join('\n');
      
      return `### ${group.file}

| 用例名 | 状态 | 耗时 |
|--------|------|------|
${rows}`;
    });
    
    let output = `## 📋 用例明细

${caseSections.join('\n\n')}`;
    
    if (needsTruncation) {
      output += `\n\n> ⚠️ 用例总数超过 200 条，完整明细见原始结果文件`;
    }
    
    return output;
  }
  
  /**
   * 生成覆盖率章节
   */
  private generateCoverage(result: TestResult): string {
    const { coverage } = result;
    
    if (!coverage) {
      return `## 📈 覆盖率

> ⚠️ 未获取覆盖率数据`;
    }
    
    const { summary, lowCoverageFiles } = coverage;
    
    let output = `## 📈 覆盖率

| 类型 | 覆盖率 |
|------|--------|
| 语句 | ${summary.statements.toFixed(1)}% |
| 分支 | ${summary.branches.toFixed(1)}% |
| 函数 | ${summary.functions.toFixed(1)}% |
| 行 | ${summary.lines.toFixed(1)}%`;
    
    if (lowCoverageFiles && lowCoverageFiles.length > 0) {
      output += `

### 低覆盖率文件

| 文件 | 语句 | 分支 | 函数 | 行 |
|------|------|------|------|------|
${lowCoverageFiles.map(f => 
  `| ${f.file} | ${f.coverage.statements.toFixed(1)}% | ${f.coverage.branches.toFixed(1)}% | ${f.coverage.functions.toFixed(1)}% | ${f.coverage.lines.toFixed(1)}% |`
).join('\n')}`;
    }
    
    return output;
  }
  
  /**
   * 生成附录
   */
  private generateAppendix(result: TestResult): string {
    const { appendix } = result;
    
    return `## 📎 附录

- 原始结果文件: \`${appendix.resultFilePath}\`
- 生成工具版本: ${appendix.generatorVersion}`;
  }
  
  /**
   * 截断消息
   */
  private truncateMessage(message: string, maxLength: number): string {
    if (message.length <= maxLength) {
      return message;
    }
    return message.substring(0, maxLength) + '...';
  }
}