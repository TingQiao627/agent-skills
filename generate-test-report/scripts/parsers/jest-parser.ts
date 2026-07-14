/**
 * Jest 解析器
 * 
 * 解析 Jest JSON 输出格式
 */

import { TestResultParser, ParsedTestResult } from '../types';
import { applySecurityFilters, filterStackTrace } from '../security-filter';

export const JestParser: TestResultParser = {
  name: 'jest',
  supportedFormats: ['.json'],
  priority: 10,
  
  canParse(filePath: string, content: string): boolean {
    if (!filePath.endsWith('.json')) return false;
    
    try {
      const data = JSON.parse(content);
      return data.success !== undefined || data.numFailedTests !== undefined;
    } catch {
      return false;
    }
  },
  
  parse(content: string): ParsedTestResult {
    const data = JSON.parse(content);
    
    const total = data.numTotalTests || 0;
    const passed = data.numPassedTests || 0;
    const failed = data.numFailedTests || 0;
    const skipped = data.numPendingTests || 0;
    const duration = data.perfStats?.runtime || 0;
    
    const testCases = extractTestCases(data);
    const failures = extractFailures(data);
    const coverage = extractCoverage(data);
    
    return {
      summary: {
        total,
        passed,
        failed,
        skipped,
        duration,
        passRate: total > 0 ? (passed / total) * 100 : 0,
      },
      testCases,
      failures,
      coverage,
      metadata: {
        framework: 'jest',
        version: data.testResults?.[0]?.perfStats?.version,
        timestamp: new Date().toISOString(),
      },
    };
  },
};

function extractTestCases(data: any): any[] {
  const cases: any[] = [];
  
  for (const testResult of data.testResults || []) {
    for (const assertionResult of testResult.assertionResults || []) {
      cases.push({
        name: assertionResult.fullName || assertionResult.title,
        file: testResult.name,
        status: assertionResult.status,
        duration: assertionResult.duration || 0,
      });
    }
  }
  
  return cases;
}

function extractFailures(data: any): any[] {
  const failures: any[] = [];
  
  for (const testResult of data.testResults || []) {
    for (const assertionResult of testResult.assertionResults || []) {
      if (assertionResult.status === 'failed') {
        failures.push({
          testCaseName: assertionResult.fullName || assertionResult.title,
          file: testResult.name,
          errorMessage: applySecurityFilters(assertionResult.failureMessages?.[0] || ''),
          stackTrace: filterStackTrace(assertionResult.failureMessages?.join('\n') || ''),
        });
      }
    }
  }
  
  return failures;
}

function extractCoverage(data: any): any | undefined {
  if (!data.coverageMap) return undefined;
  
  // 简化覆盖率提取（实际实现需遍历 coverageMap）
  return {
    statements: 0,
    branches: 0,
    functions: 0,
    lines: 0,
  };
}