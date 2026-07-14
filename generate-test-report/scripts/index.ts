/**
 * 测试报告生成器 - 入口模块
 * 
 * 协调框架检测、测试执行、结果解析、报告生成
 */

import * as fs from 'fs';
import * as path from 'path';
import { TestReportConfig } from './types';
import { detectTestFramework, getFallbackCommand } from './detector';
import { parseTestResult } from './parsers';
import { generateMarkdownReport } from './generators/markdown-generator';
import { validateCommandSafety } from './security-filter';

/**
 * 主入口：生成测试报告
 */
export async function generateTestReport(config: TestReportConfig): Promise<string> {
  const projectRoot = process.cwd();
  
  // 模式选择
  if (config.resultFile) {
    // 解析模式
    return await parseAndGenerate(config.resultFile, config);
  } else {
    // 执行模式
    return await executeAndGenerate(projectRoot, config);
  }
}

/**
 * 解析模式：仅解析已有结果文件
 */
async function parseAndGenerate(resultFile: string, config: TestReportConfig): Promise<string> {
  if (!fs.existsSync(resultFile)) {
    throw new Error(`结果文件不存在: ${resultFile}`);
  }
  
  const content = fs.readFileSync(resultFile, 'utf-8');
  const result = parseTestResult(resultFile, content);
  const report = generateMarkdownReport(result);
  
  const outputPath = await saveReport(report, config);
  return outputPath;
}

/**
 * 执行模式：运行测试并生成报告
 */
async function executeAndGenerate(projectRoot: string, config: TestReportConfig): Promise<string> {
  // 框架检测
  const framework = detectTestFramework(projectRoot);
  
  if (!framework) {
    const fallbacks = getFallbackCommand();
    throw new Error(
      `未检测到测试框架。支持的框架: Jest, Vitest, pytest, JUnit XML\n` +
      `尝试以下命令: ${fallbacks.join(', ')}`
    );
  }
  
  // 命令安全验证
  const command = config.testCommand || framework.testCommand;
  const safety = validateCommandSafety(command);
  
  if (!safety.safe) {
    throw new Error(`安全验证失败: ${safety.reason}`);
  }
  
  // 实际执行逻辑（需配合运行时）
  console.log(`检测到框架: ${framework.name}`);
  console.log(`建议命令: ${command}`);
  
  // 返回占位符（实际执行需集成测试运行器）
  return 'Execution mode requires runtime integration';
}

/**
 * 保存报告到文件
 */
async function saveReport(report: string, config: TestReportConfig): Promise<string> {
  const outputDir = config.outputPath || 'reports';
  
  if (!fs.existsSync(outputDir)) {
    fs.mkdirSync(outputDir, { recursive: true });
  }
  
  const timestamp = new Date().toISOString().replace(/[:.]/g, '-').slice(0, 19);
  const filename = `test-report-${timestamp}.md`;
  const outputPath = path.join(outputDir, filename);
  
  fs.writeFileSync(outputPath, report, 'utf-8');
  
  return outputPath;
}

// CLI 入口
if (require.main === module) {
  const config: TestReportConfig = {
    outputFormat: 'markdown',
    outputPath: 'reports',
    coverage: 'auto',
  };
  
  generateTestReport(config)
    .then(path => console.log(`报告已生成: ${path}`))
    .catch(err => console.error(`生成失败: ${err.message}`));
}