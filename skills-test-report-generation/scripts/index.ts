/**
 * 主入口
 */

import * as path from 'path';
import * as fs from 'fs';
import { detectTestFramework, DetectedFramework } from './framework-detector';
import { executeTest, readResultFile, detectResultFormat, TestExecutionResult } from './test-executor';
import { ParserFactory, ParsedTestResult } from './parsers/base-parser';
import { JestParser } from './parsers/jest-parser';
import { JUnitParser } from './parsers/junit-parser';
import { generateMarkdownReport, saveReport, ReportOptions } from './report-generator';

// 注册解析器
ParserFactory.register(new JestParser());
ParserFactory.register(new JUnitParser());

export interface TestReportOptions {
  testCommand?: string;
  resultFile?: string;
  outputFormat?: 'markdown' | 'html' | 'json';
  outputPath?: string;
  coverage?: 'auto' | 'on' | 'off';
  failThreshold?: number;
  mode?: 'execution' | 'parse';
}

export interface TestReportResult {
  success: boolean;
  reportPath?: string;
  summary?: {
    total: number;
    passed: number;
    failed: number;
    passRate: number;
    success: boolean;
  };
  topFailures?: Array<{ name: string; error: string }>;
  error?: string;
}

/**
 * 生成测试报告（主入口）
 */
export async function generateTestReport(
  options: TestReportOptions = {}
): Promise<TestReportResult> {
  const mode = options.mode || (options.resultFile ? 'parse' : 'execution');
  
  let resultFile: string | undefined;
  let detected: DetectedFramework | null = null;
  let executionResult: TestExecutionResult | undefined;
  
  try {
    if (mode === 'execution') {
      // 执行模式：检测并运行测试
      detected = detectTestFramework({ explicitCommand: options.testCommand });
      
      if (!detected) {
        return {
          success: false,
          error: '无法检测到测试框架，请通过 testCommand 参数指定测试命令',
        };
      }
      
      executionResult = await executeTest(detected);
      
      if (executionResult.error) {
        return {
          success: false,
          error: executionResult.error,
        };
      }
      
      resultFile = executionResult.resultFile;
    } else {
      // 解析模式：读取已有结果文件
      resultFile = options.resultFile;
    }
    
    if (!resultFile) {
      return {
        success: false,
        error: '未找到测试结果文件',
      };
    }
    
    // 读取结果文件
    const readResult = readResultFile(resultFile);
    if (!readResult.success || !readResult.content) {
      return {
        success: false,
        error: readResult.error || '读取结果文件失败',
      };
    }
    
    // 检测格式并解析
    const format = detectResultFormat(readResult.content, resultFile);
    const parserName = format === 'jest-json' ? 'jest-json' : 'junit-xml';
    const parser = ParserFactory.get(parserName);
    
    if (!parser) {
      return {
        success: false,
        error: `不支持的格式: ${format}`,
      };
    }
    
    let parsed: ParsedTestResult;
    try {
      parsed = parser.parse(readResult.content, resultFile);
    } catch (parseError: unknown) {
      return {
        success: false,
        error: `解析失败: ${(parseError as Error).message}`,
      };
    }
    
    // 生成报告
    const reportContent = generateMarkdownReport(parsed, {
      outputPath: options.outputPath,
      failThreshold: options.failThreshold,
    });
    
    const reportPath = saveReport(reportContent, { outputPath: options.outputPath });
    
    // 提取前 3 条失败
    const topFailures: Array<{ name: string; error: string }> = [];
    for (const file of parsed.files) {
      for (const tc of file.testCases) {
        if (tc.status === 'failed' && tc.error) {
          topFailures.push({ name: tc.name, error: tc.error.message });
          if (topFailures.length >= 3) break;
        }
      }
      if (topFailures.length >= 3) break;
    }
    
    return {
      success: true,
      reportPath,
      summary: {
        total: parsed.summary.total,
        passed: parsed.summary.passed,
        failed: parsed.summary.failed,
        passRate: parsed.summary.passRate,
        success: parsed.summary.success,
      },
      topFailures: topFailures.length > 0 ? topFailures : undefined,
    };
  } catch (error: unknown) {
    return {
      success: false,
      error: `生成报告失败: ${(error as Error).message}`,
    };
  }
}

export { detectTestFramework };
export { JestParser, JUnitParser };
export { generateMarkdownReport };