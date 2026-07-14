/**
 * Vitest 解析器
 * 
 * 解析 Vitest JSON 输出格式
 */

import { TestResultParser, ParsedTestResult } from '../types';
import { applySecurityFilters, filterStackTrace } from '../security-filter';

export const VitestParser: TestResultParser = {
  name: 'vitest',
  supportedFormats: ['.json'],
  priority: 15,
  
  canParse(filePath: string, content: string): boolean {
    if (!filePath.endsWith('.json')) return false;
    
    try {
      const data = JSON.parse(content);
      return data.testResults !== undefined || Array.isArray(data);
    } catch {
      return false;
    }
  },
  
  parse(content: string): ParsedTestResult {
    const data = JSON.parse(content);
    
    // Vitest JSON 结构适配
    const results = Array.isArray(data) ? data : [data];
    
    let total = 0, passed = 0, failed = 0, skipped = 0, duration = 0;
    const testCases: any[] = [];
    const failures: any[] = [];
    
    for (const result of results) {
      for (const test of result.testResults || []) {
        total++;
        duration += test.duration || 0;
        
        const status = mapStatus(test.status);
        if (status === 'passed') passed++;
        else if (status === 'failed') failed++;
        else skipped++;
        
        testCases.push({
          name: test.name,
          file: result.name,
          status,
          duration: test.duration || 0,
        });
        
        if (test.status === 'failed') {
          failures.push({
            testCaseName: test.name,
            file: result.name,
            errorMessage: applySecurityFilters(test.errorMessage || ''),
            stackTrace: filterStackTrace(test.errorStack || ''),
          });
        }
      }
    }
    
    return {
      summary: { total, passed, failed, skipped, duration, passRate: total > 0 ? (passed / total) * 100 : 0 },
      testCases,
      failures,
      metadata: { framework: 'vitest', timestamp: new Date().toISOString() },
    };
  },
};

function mapStatus(status: string): 'passed' | 'failed' | 'skipped' {
  switch (status) {
    case 'passed': return 'passed';
    case 'failed': return 'failed';
    default: return 'skipped';
  }
}