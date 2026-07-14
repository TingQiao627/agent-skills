/**
 * JUnit XML 解析器（通用）
 */

import { BaseParser, ParsedTestResult, TestCaseResult, TestFileResult } from './base-parser';

export class JUnitParser extends BaseParser {
  readonly name = 'junit-xml';

  parse(content: string, filePath?: string): ParsedTestResult {
    // 简单 XML 解析（不依赖外部库）
    const testSuites = this.parseTestSuites(content);
    
    const files: TestFileResult[] = [];
    let totalDuration = 0;
    let total = 0;
    let passed = 0;
    let failed = 0;
    let skipped = 0;
    
    for (const suite of testSuites) {
      const testCases: TestCaseResult[] = [];
      
      for (const tc of suite.testcases) {
        total++;
        
        const result: TestCaseResult = {
          name: tc.name,
          file: suite.name || tc.classname || 'unknown',
          status: tc.status,
          duration: Math.round((tc.time || 0) * 1000),
        };
        
        if (tc.error) {
          result.error = {
            message: this.sanitizeMessage(tc.error.message || ''),
            stack: this.truncateStack(tc.error.stack),
          };
        }
        
        testCases.push(result);
        totalDuration += result.duration;
        
        switch (tc.status) {
          case 'passed': passed++; break;
          case 'failed': failed++; break;
          case 'skipped': skipped++; break;
        }
      }
      
      files.push({
        file: suite.name || 'unknown',
        testCases,
        duration: Math.round((suite.time || 0) * 1000),
      });
    }
    
    return {
      framework: 'JUnit',
      summary: {
        total,
        passed,
        failed,
        skipped,
        pending: 0,
        duration: totalDuration,
        passRate: this.calculatePassRate(passed, total),
        success: failed === 0,
      },
      files,
      metadata: {
        resultFile: filePath,
      },
    };
  }

  private parseTestSuites(content: string) {
    const suites: Array<{
      name: string;
      time: number;
      testcases: Array<{
        name: string;
        classname?: string;
        status: 'passed' | 'failed' | 'skipped';
        time: number;
        error?: { message: string; stack?: string };
      }>;
    }> = [];
    
    // 提取 testsuite 标签
    const suiteRegex = /<testsuite[^>]*>/g;
    const testCaseRegex = /<testcase[^>]*>([\s\S]*?)<\/testcase>/gi;
    const attrRegex = /(\w+)="([^"]*)"/g;
    
    let suiteMatch;
    const suiteIndices: number[] = [];
    while ((suiteMatch = suiteRegex.exec(content)) !== null) {
      suiteIndices.push(suiteMatch.index);
    }
    
    // 简化处理：提取所有 testcase
    const testCases: typeof suites[0]['testcases'] = [];
    let tcMatch;
    while ((tcMatch = testCaseRegex.exec(content)) !== null) {
      const tcTag = tcMatch[0];
      const tcContent = tcMatch[1];
      
      const attrs = this.parseAttrs(tcTag);
      
      let status: 'passed' | 'failed' | 'skipped' = 'passed';
      let error: { message: string; stack?: string } | undefined;
      
      if (tcContent.includes('<failure') || tcContent.includes('<error')) {
        status = 'failed';
        const msgMatch = /<(?:failure|error)[^>]*message="([^"]*)"/.exec(tcContent);
        const stackMatch = /<(?:failure|error)[^>]*><!\[CDATA\[([\s\S]*?)\]\]>/i.exec(tcContent);
        error = {
          message: msgMatch ? msgMatch[1] : 'Unknown error',
          stack: stackMatch ? stackMatch[1] : undefined,
        };
      } else if (tcContent.includes('<skipped')) {
        status = 'skipped';
      }
      
      testCases.push({
        name: attrs.name || 'unknown',
        classname: attrs.classname,
        status,
        time: parseFloat(attrs.time || '0'),
        error,
      });
    }
    
    // 单一 suite
    suites.push({
      name: this.extractAttr(content, 'testsuites', 'name') || 'Test Suite',
      time: parseFloat(this.extractAttr(content, 'testsuites', 'time') || '0'),
      testcases,
    });
    
    return suites;
  }

  private parseAttrs(tag: string): Record<string, string> {
    const attrs: Record<string, string> = {};
    const regex = /(\w+)="([^"]*)"/g;
    let match;
    while ((match = regex.exec(tag)) !== null) {
      attrs[match[1]] = match[2];
    }
    return attrs;
  }

  private extractAttr(content: string, tag: string, attr: string): string | null {
    const regex = new RegExp(`<${tag}[^>]*${attr}="([^"]*)"`, 'i');
    const match = regex.exec(content);
    return match ? match[1] : null;
  }
}