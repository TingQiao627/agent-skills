/**
 * JUnit XML 解析器
 * 
 * 解析 JUnit XML 格式（通用兜底）
 */

import { TestResultParser, ParsedTestResult } from '../types';
import { applySecurityFilters, filterStackTrace } from '../security-filter';

export const JUnitXmlParser: TestResultParser = {
  name: 'junit-xml',
  supportedFormats: ['.xml'],
  priority: 100,
  
  canParse(filePath: string, content: string): boolean {
    if (!filePath.endsWith('.xml')) return false;
    return content.includes('<testsuite') || content.includes('<testcase');
  },
  
  parse(content: string): ParsedTestResult {
    // 简化 XML 解析（实际需使用 XML 解析库）
    const testCases = parseTestCases(content);
    const summary = calculateSummary(testCases);
    const failures = extractFailures(content);
    
    return {
      summary,
      testCases,
      failures,
      metadata: { framework: 'junit-xml', timestamp: new Date().toISOString() },
    };
  },
};

function parseTestCases(xml: string): any[] {
  // 简化实现：实际需使用 XML 解析器
  const cases: any[] = [];
  const testCaseRegex = /<testcase[^>]*name="([^"]+)"[^>]*>/g;
  
  let match;
  while ((match = testCaseRegex.exec(xml)) !== null) {
    cases.push({
      name: match[1],
      file: '',
      status: 'passed',
      duration: 0,
    });
  }
  
  return cases;
}

function calculateSummary(testCases: any[]): any {
  const total = testCases.length;
  const passed = testCases.filter(tc => tc.status === 'passed').length;
  const failed = testCases.filter(tc => tc.status === 'failed').length;
  const skipped = total - passed - failed;
  
  return {
    total,
    passed,
    failed,
    skipped,
    duration: 0,
    passRate: total > 0 ? (passed / total) * 100 : 0,
  };
}

function extractFailures(xml: string): any[] {
  const failures: any[] = [];
  const failureRegex = /<failure[^>]*>\s*<!\[CDATA\[([\s\S]*?)\]\]>\s*<\/failure>/g;
  
  let match;
  while ((match = failureRegex.exec(xml)) !== null) {
    failures.push({
      testCaseName: 'Unknown',
      file: '',
      errorMessage: applySecurityFilters(match[1].split('\n')[0] || ''),
      stackTrace: filterStackTrace(match[1]),
    });
  }
  
  return failures;
}