/**
 * Jest JSON Reporter 解析器
 */

import { BaseParser, TestResult, FailureCase, TestCaseGroup, TestCase } from './base';

interface JestAssertionResult {
  ancestorTitles: string[];
  fullName: string;
  status: 'passed' | 'failed' | 'pending' | 'skipped';
  title: string;
  duration?: number;
  failureMessages?: string[];
}

interface JestTestResult {
  name: string;
  status: 'passed' | 'failed' | 'pending' | 'skipped';
  message?: string;
  assertionResults: JestAssertionResult[];
  startTime: number;
  endTime: number;
}

interface JestJsonReport {
  success: boolean;
  startTime: number;
  numTotalTests: number;
  numPassedTests: number;
  numFailedTests: number;
  numPendingTests: number;
  numTodoTests?: number;
  testResults: JestTestResult[];
  coverageMap?: any;
}

export class JestParser extends BaseParser {
  name = 'JestParser';
  supportedFormats = ['jest-json', 'jest'];
  
  canParse(filePath: string, content?: string): boolean {
    // 检查文件扩展名
    if (filePath.includes('jest') && filePath.endsWith('.json')) {
      return true;
    }
    
    // 检查内容格式
    if (content) {
      try {
        const data = JSON.parse(content);
        return (
          typeof data.numTotalTests === 'number' &&
          typeof data.numPassedTests === 'number' &&
          Array.isArray(data.testResults)
        );
      } catch {
        return false;
      }
    }
    
    return false;
  }
  
  async parse(filePath: string, content: string): Promise<TestResult> {
    let data: JestJsonReport;
    
    try {
      data = JSON.parse(content);
    } catch (error) {
      throw new Error('RESULT_FILE_INVALID: Jest JSON 文件格式无效');
    }
    
    // 提取失败用例
    const failures: FailureCase[] = [];
    
    for (const testResult of data.testResults) {
      for (const assertion of testResult.assertionResults) {
        if (assertion.status === 'failed') {
          failures.push({
            name: assertion.fullName,
            file: testResult.name,
            error: {
              message: this.sanitizeMessage(
                assertion.failureMessages?.join('\n') || 'Unknown error'
              ),
              stack: assertion.failureMessages?.[0]
                ? this.truncateStack(assertion.failureMessages[0])
                : undefined
            },
            duration: assertion.duration
          });
        }
      }
    }
    
    // 按文件分组用例
    const testCases: TestCaseGroup[] = data.testResults.map(testResult => {
      const cases: TestCase[] = testResult.assertionResults.map(assertion => ({
        name: assertion.fullName,
        status: assertion.status === 'pending' ? 'skipped' : assertion.status,
        duration: assertion.duration || 0
      }));
      
      return {
        file: testResult.name,
        cases,
        duration: testResult.endTime - testResult.startTime
      };
    });
    
    const skipped = data.numPendingTests + (data.numTodoTests || 0);
    const passed = data.numPassedTests;
    const failed = data.numFailedTests;
    const total = data.numTotalTests;
    
    return {
      metadata: {
        projectName: process.cwd().split('/').pop() || 'Unknown',
        generatedAt: new Date().toISOString(),
        testCommand: 'npm test -- --json',
        framework: 'Jest',
        environment: `Node ${process.version}`
      },
      summary: {
        total,
        passed,
        failed,
        skipped,
        duration: Date.now() - data.startTime,
        passRate: this.calculatePassRate(passed, total),
        status: failed > 0 ? 'failed' : 'passed'
      },
      failures,
      testCases,
      appendix: {
        resultFilePath: filePath,
        generatorVersion: 'test-report-generator v1.0'
      }
    };
  }
}