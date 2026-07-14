/**
 * Vitest JSON Reporter 解析器
 */

import { BaseParser, TestResult, FailureCase, TestCaseGroup, TestCase } from './base';

interface VitestAssertionResult {
  name: string;
  fullName?: string;
  status: 'passed' | 'failed' | 'skipped' | 'pending';
  duration: number;
  error?: {
    message: string;
    stack?: string;
  };
}

interface VitestTestResult {
  name: string;
  status: 'passed' | 'failed' | 'skipped' | 'pending';
  duration: number;
  assertionResults: VitestAssertionResult[];
}

interface VitestJsonReport {
  testResults: VitestTestResult[];
  success: boolean;
  duration: number;
  numTotalTests?: number;
  numPassedTests?: number;
  numFailedTests?: number;
  numSkippedTests?: number;
}

export class VitestParser extends BaseParser {
  name = 'VitestParser';
  supportedFormats = ['vitest-json', 'vitest'];
  
  canParse(filePath: string, content?: string): boolean {
    // 检查文件扩展名
    if (filePath.includes('vitest') && filePath.endsWith('.json')) {
      return true;
    }
    
    // 检查内容格式
    if (content) {
      try {
        const data = JSON.parse(content);
        return (
          Array.isArray(data.testResults) &&
          data.testResults[0]?.assertionResults !== undefined
        );
      } catch {
        return false;
      }
    }
    
    return false;
  }
  
  async parse(filePath: string, content: string): Promise<TestResult> {
    let data: VitestJsonReport;
    
    try {
      data = JSON.parse(content);
    } catch (error) {
      throw new Error('RESULT_FILE_INVALID: Vitest JSON 文件格式无效');
    }
    
    // 统计用例
    let total = 0;
    let passed = 0;
    let failed = 0;
    let skipped = 0;
    const failures: FailureCase[] = [];
    const testCases: TestCaseGroup[] = [];
    
    for (const testResult of data.testResults) {
      const cases: TestCase[] = [];
      
      for (const assertion of testResult.assertionResults) {
        total++;
        
        const status = assertion.status === 'pending' ? 'skipped' : assertion.status;
        
        if (status === 'passed') passed++;
        else if (status === 'failed') {
          failed++;
          
          failures.push({
            name: assertion.fullName || assertion.name,
            file: testResult.name,
            error: {
              message: this.sanitizeMessage(assertion.error?.message || 'Unknown error'),
              stack: assertion.error?.stack
                ? this.truncateStack(assertion.error.stack)
                : undefined
            },
            duration: assertion.duration
          });
        }
        else if (status === 'skipped') skipped++;
        
        cases.push({
          name: assertion.fullName || assertion.name,
          status,
          duration: assertion.duration || 0
        });
      }
      
      testCases.push({
        file: testResult.name,
        cases,
        duration: testResult.duration
      });
    }
    
    return {
      metadata: {
        projectName: process.cwd().split('/').pop() || 'Unknown',
        generatedAt: new Date().toISOString(),
        testCommand: 'npm test -- --reporter=json',
        framework: 'Vitest',
        environment: `Node ${process.version}`
      },
      summary: {
        total,
        passed,
        failed,
        skipped,
        duration: data.duration || 0,
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