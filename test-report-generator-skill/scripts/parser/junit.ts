/**
 * JUnit XML 解析器（通用）
 * 支持 pytest、Maven、Gradle 等生成的 JUnit XML 格式
 */

import { BaseParser, TestResult, FailureCase, TestCaseGroup, TestCase } from './base';

interface JUnitTestSuite {
  name: string;
  tests: number;
  failures: number;
  errors?: number;
  skipped?: number;
  time: number;
  testcases: JUnitTestCase[];
}

interface JUnitTestCase {
  name: string;
  classname?: string;
  time: number;
  status: 'passed' | 'failed' | 'skipped' | 'error';
  failure?: {
    message: string;
    type: string;
    stack?: string;
  };
  skipped?: {
    message?: string;
  };
}

export class JUnitParser extends BaseParser {
  name = 'JUnitParser';
  supportedFormats = ['junit-xml', 'junit', 'pytest-xml'];
  
  canParse(filePath: string, content?: string): boolean {
    // 检查文件扩展名
    if (filePath.endsWith('.xml') && filePath.includes('junit') || filePath.includes('test')) {
      return true;
    }
    
    // 检查内容格式
    if (content) {
      return content.includes('<testsuite') || content.includes('<?xml');
    }
    
    return false;
  }
  
  async parse(filePath: string, content: string): Promise<TestResult> {
    try {
      const testSuites = this.parseJUnitXml(content);
      return this.buildTestResult(filePath, testSuites);
    } catch (error) {
      throw new Error('RESULT_FILE_INVALID: JUnit XML 文件格式无效');
    }
  }
  
  /**
   * 解析 JUnit XML 内容
   */
  private parseJUnitXml(xml: string): JUnitTestSuite[] {
    const testSuites: JUnitTestSuite[] = [];
    
    // 简单的正则解析（生产环境建议使用 fast-xml-parser）
    const testsuiteRegex = /<testsuite[^>]*>/g;
    let match;
    
    while ((match = testsuiteRegex.exec(xml)) !== null) {
      const testsuiteTag = match[0];
      
      // 提取属性
      const name = this.extractAttribute(testsuiteTag, 'name') || 'Unknown';
      const tests = parseInt(this.extractAttribute(testsuiteTag, 'tests') || '0');
      const failures = parseInt(this.extractAttribute(testsuiteTag, 'failures') || '0');
      const errors = parseInt(this.extractAttribute(testsuiteTag, 'errors') || '0');
      const skipped = parseInt(this.extractAttribute(testsuiteTag, 'skipped') || '0');
      const time = parseFloat(this.extractAttribute(testsuiteTag, 'time') || '0');
      
      // 提取 testcases
      const testsuiteContent = this.extractContent(xml, 'testsuite', match.index);
      const testcases = this.parseTestCases(testsuiteContent);
      
      testSuites.push({
        name,
        tests,
        failures,
        errors,
        skipped,
        time,
        testcases
      });
    }
    
    return testSuites;
  }
  
  /**
   * 解析 testcases
   */
  private parseTestCases(content: string): JUnitTestCase[] {
    const testcases: JUnitTestCase[] = [];
    const testcaseRegex = /<testcase[^>]*>([\s\S]*?)<\/testcase>/g;
    let match;
    
    while ((match = testcaseRegex.exec(content)) !== null) {
      const testcaseTag = match[0];
      const testcaseContent = match[1];
      
      const name = this.extractAttribute(testcaseTag, 'name') || 'Unknown';
      const classname = this.extractAttribute(testcaseTag, 'classname');
      const time = parseFloat(this.extractAttribute(testcaseTag, 'time') || '0');
      
      let status: JUnitTestCase['status'] = 'passed';
      let failure: JUnitTestCase['failure'];
      let skippedData: JUnitTestCase['skipped'];
      
      if (testcaseContent.includes('<failure')) {
        status = 'failed';
        const failureMessage = this.extractContent(testcaseContent, 'failure');
        const failureType = this.extractAttribute(
          testcaseContent.match(/<failure[^>]*>/)?.[0] || '',
          'type'
        ) || 'AssertionError';
        
        failure = {
          message: this.sanitizeMessage(failureMessage),
          type: failureType,
          stack: this.truncateStack(failureMessage)
        };
      } else if (testcaseContent.includes('<error')) {
        status = 'error';
        const errorMessage = this.extractContent(testcaseContent, 'error');
        failure = {
          message: this.sanitizeMessage(errorMessage),
          type: 'Error',
          stack: this.truncateStack(errorMessage)
        };
      } else if (testcaseContent.includes('<skipped')) {
        status = 'skipped';
        const skippedMessage = this.extractAttribute(
          testcaseContent.match(/<skipped[^>]*>/)?.[0] || '',
          'message'
        );
        skippedData = {
          message: skippedMessage
        };
      }
      
      testcases.push({
        name,
        classname,
        time,
        status,
        failure,
        skipped: skippedData
      });
    }
    
    return testcases;
  }
  
  /**
   * 提取 XML 属性
   */
  private extractAttribute(tag: string, attr: string): string | null {
    const regex = new RegExp(`${attr}="([^"]*)"`, 'i');
    const match = tag.match(regex);
    return match ? match[1] : null;
  }
  
  /**
   * 提取 XML 标签内容
   */
  private extractContent(xml: string, tag: string, startIndex: number = 0): string {
    const startRegex = new RegExp(`<${tag}[^>]*>`, 'g');
    startRegex.lastIndex = startIndex;
    
    const startMatch = startRegex.exec(xml);
    if (!startMatch) return '';
    
    const start = startMatch.index + startMatch[0].length;
    const endTag = `</${tag}>`;
    const end = xml.indexOf(endTag, start);
    
    if (end === -1) return '';
    
    return xml.substring(start, end).trim();
  }
  
  /**
   * 构建 TestResult
   */
  private buildTestResult(filePath: string, testSuites: JUnitTestSuite[]): TestResult {
    let total = 0;
    let passed = 0;
    let failed = 0;
    let skipped = 0;
    let duration = 0;
    const failures: FailureCase[] = [];
    const testCases: TestCaseGroup[] = [];
    
    for (const suite of testSuites) {
      total += suite.tests;
      failed += suite.failures + (suite.errors || 0);
      skipped += suite.skipped || 0;
      duration += suite.time;
      
      const cases: TestCase[] = [];
      
      for (const testcase of suite.testcases) {
        if (testcase.status === 'passed') passed++;
        
        if (testcase.failure) {
          failures.push({
            name: testcase.name,
            file: testcase.classname || suite.name,
            error: {
              message: testcase.failure.message,
              stack: testcase.failure.stack
            },
            duration: testcase.time
          });
        }
        
        cases.push({
          name: testcase.name,
          status: testcase.status === 'error' ? 'failed' : testcase.status,
          duration: testcase.time
        });
      }
      
      testCases.push({
        file: suite.name,
        cases,
        duration: suite.time
      });
    }
    
    passed = total - failed - skipped;
    
    return {
      metadata: {
        projectName: process.cwd().split('/').pop() || 'Unknown',
        generatedAt: new Date().toISOString(),
        testCommand: 'Test execution completed',
        framework: 'JUnit XML',
        environment: 'Universal'
      },
      summary: {
        total,
        passed,
        failed,
        skipped,
        duration: Math.round(duration * 1000),
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