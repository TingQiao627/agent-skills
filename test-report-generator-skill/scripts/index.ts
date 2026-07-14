#!/usr/bin/env node

/**
 * 测试报告生成器 CLI 入口
 * 
 * 用法：
 *   node index.js --file test-results.json --output reports/
 *   node index.js --parse junit.xml --output reports/
 */

import * as fs from 'fs';
import * as path from 'path';
import { createDefaultRegistry } from './parser';
import { ReportGenerator } from './generator';
import { FrameworkDetector } from './detector';

interface CliOptions {
  file?: string;
  output: string;
  format: 'markdown' | 'html' | 'json';
  mode: 'parse' | 'execute';
  command?: string;
}

/**
 * 主入口函数
 */
async function main() {
  const args = parseArgs();
  
  try {
    const result = await processTestResults(args);
    const report = generateReport(result, args.format);
    const outputPath = writeReport(report, args.output, args.format);
    
    printSummary(result, outputPath);
  } catch (error: any) {
    console.error('❌ 错误:', error.message);
    process.exit(1);
  }
}

/**
 * 解析命令行参数
 */
function parseArgs(): CliOptions {
  const args = process.argv.slice(2);
  const options: CliOptions = {
    output: 'reports/',
    format: 'markdown',
    mode: 'parse'
  };
  
  for (let i = 0; i < args.length; i++) {
    const arg = args[i];
    
    if (arg === '--file' || arg === '-f') {
      options.file = args[++i];
    } else if (arg === '--output' || arg === '-o') {
      options.output = args[++i];
    } else if (arg === '--format' || arg === '-m') {
      options.format = args[++i] as any;
    } else if (arg === '--parse') {
      options.mode = 'parse';
      options.file = args[++i];
    } else if (arg === '--execute') {
      options.mode = 'execute';
    } else if (arg === '--command' || arg === '-c') {
      options.command = args[++i];
    }
  }
  
  return options;
}

/**
 * 处理测试结果
 */
async function processTestResults(options: CliOptions) {
  const registry = createDefaultRegistry();
  
  if (options.mode === 'parse' && options.file) {
    // 解析模式
    const content = fs.readFileSync(options.file, 'utf-8');
    const parser = registry.findParser(options.file, content);
    
    if (!parser) {
      throw new Error(`不支持的文件格式: ${options.file}`);
    }
    
    return parser.parse(options.file, content);
  } else {
    // 执行模式（简化实现）
    throw new Error('执行模式需要完整的测试环境');
  }
}

/**
 * 生成报告
 */
function generateReport(result: any, format: string): string {
  const generator = new ReportGenerator();
  return generator.generate(result, format as any);
}

/**
 * 写入报告文件
 */
function writeReport(content: string, outputDir: string, format: string): string {
  // 确保输出目录存在
  if (!fs.existsSync(outputDir)) {
    fs.mkdirSync(outputDir, { recursive: true });
  }
  
  // 生成文件名
  const timestamp = new Date().toISOString().replace(/[:.]/g, '-').slice(0, 19);
  const ext = format === 'markdown' ? 'md' : format === 'json' ? 'json' : 'html';
  const filename = `test-report-${timestamp}.${ext}`;
  const outputPath = path.join(outputDir, filename);
  
  // 写入文件
  fs.writeFileSync(outputPath, content, 'utf-8');
  
  return outputPath;
}

/**
 * 打印摘要
 */
function printSummary(result: any, outputPath: string) {
  const { summary, failures } = result;
  const statusIcon = summary.status === 'passed' ? '✅' : '❌';
  
  console.log('\n✅ 测试报告已生成\n');
  console.log(`📄 报告路径: ${outputPath}\n`);
  console.log('📊 结果摘要:');
  console.log(`- 用例总数: ${summary.total}`);
  console.log(`- 通过: ${summary.passed} ✅`);
  console.log(`- 失败: ${summary.failed} ❌`);
  console.log(`- 跳过: ${summary.skipped} ⏭️`);
  console.log(`- 通过率: ${summary.passRate}%`);
  
  if (failures.length > 0) {
    console.log('\n❌ 关键失败用例:');
    failures.slice(0, 3).forEach((f: any, i: number) => {
      console.log(`${i + 1}. ${f.name} - ${f.error.message}`);
    });
  }
}

// 执行主函数
if (require.main === module) {
  main().catch(console.error);
}

export { main, processTestResults, generateReport, writeReport };