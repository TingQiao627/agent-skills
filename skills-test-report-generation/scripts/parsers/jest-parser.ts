/**
 * Jest JSON 解析器
 */

import { BaseParser, ParsedTestResult, TestCaseResult, TestFileResult } from './base-parser';

interface JestJsonResult {
  success: boolean;
  numTotalTests: number;
  numPassedTests: number;
  numFailedTests: number;
  numPendingTests: number;
  numTodoTests: number;
  startTime: number;
  testResults: JestTestResult[];
  coverageMap?: Record<string, JestCoverage>;
}

interface JestTestResult {
  name: string;
  status: 'passed' | 'failed' | 'skipped' | 'pending' | 'todo' | 'focused';
  assertionResults: JestAssertionResult[];
  startTime: number;
  endTime: number;
  runDuration?: number;
}

interface JestAssertionResult {
  ancestorTitles: string[];
  fullName: string;
  status: 'passed' | 'failed' | 'skipped' | 'pending' | 'todo' | 'focused';
  title: string;
  duration?: number;
  failureMessages?: string[];
}

interface JestCoverage {
  lines: { total: number; covered: number; percentage: number };
  statements: { total: number; covered: number; percentage: number };
  branches: { total: number; covered: number; percentage: number };
  functions: { total: number; covered: number; percentage: number };
}

export class JestParser extends BaseParser {
  readonly name = 'jest-json';

  parse(content: string, filePath?: string): ParsedTestResult {
    let data: JestJsonResult;
    
    try {
      data = JSON.parse(content);
    } catch {
      throw new Error('无效的 Jest JSON 格式');
    }
    
    const files: TestFileResult[] = data.testResults.map(result => ({
      file: result.name,
      duration: result.runDuration || (result.endTime - result.startTime) || 0,
      testCases: result.assertionResults.map(assertion => ({
        name: assertion.fullName,
        file: result.name,
        status: this.mapStatus(assertion.status),
        duration: assertion.duration || 0,
        error: assertion.failureMessages && assertion.failureMessages.length > 0
          ? {
              message: this.sanitizeMessage(assertion.failureMessages[0]),
              stack: this.truncateStack(assertion.failureMessages.join('\n')),
            }
          : undefined,
      })),
    }));
    
    const totalDuration = files.reduce((sum, f) => sum + f.duration, 0);
    
    return {
      framework: 'Jest',
      summary: {
        total: data.numTotalTests,
        passed: data.numPassedTests,
        failed: data.numFailedTests,
        skipped: data.numPendingTests,
        pending: data.numTodoTests,
        duration: totalDuration,
        passRate: this.calculatePassRate(data.numPassedTests, data.numTotalTests),
        success: data.success,
      },
      files,
      coverage: data.coverageMap ? this.parseCoverage(data.coverageMap) : undefined,
      metadata: {
        startTime: new Date(data.startTime),
        resultFile: filePath,
      },
    };
  }

  private mapStatus(status: string): 'passed' | 'failed' | 'skipped' | 'pending' {
    switch (status) {
      case 'passed': return 'passed';
      case 'failed': return 'failed';
      case 'skipped': return 'skipped';
      case 'pending':
      case 'todo':
      case 'focused': return 'pending';
      default: return 'passed';
    }
  }

  private parseCoverage(coverageMap: Record<string, JestCoverage>) {
    const entries = Object.entries(coverageMap);
    const totals = entries.reduce(
      (acc, [, cov]) => ({
        lines: { total: acc.lines.total + cov.lines.total, covered: acc.lines.covered + cov.lines.covered, percentage: 0 },
        statements: { total: acc.statements.total + cov.statements.total, covered: acc.statements.covered + cov.statements.covered, percentage: 0 },
        branches: { total: acc.branches.total + cov.branches.total, covered: acc.branches.covered + cov.branches.covered, percentage: 0 },
        functions: { total: acc.functions.total + cov.functions.total, covered: acc.functions.covered + cov.functions.covered, percentage: 0 },
      }),
      { lines: { total: 0, covered: 0, percentage: 0 }, statements: { total: 0, covered: 0, percentage: 0 }, branches: { total: 0, covered: 0, percentage: 0 }, functions: { total: 0, covered: 0, percentage: 0 } }
    );
    
    totals.lines.percentage = this.calculatePassRate(totals.lines.covered, totals.lines.total);
    totals.statements.percentage = this.calculatePassRate(totals.statements.covered, totals.statements.total);
    totals.branches.percentage = this.calculatePassRate(totals.branches.covered, totals.branches.total);
    totals.functions.percentage = this.calculatePassRate(totals.functions.covered, totals.functions.total);
    
    return totals;
  }
}