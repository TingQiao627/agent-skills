/**
 * 类型定义 - 测试报告生成器
 * 
 * 定义解析器接口、报告结构、配置项等核心类型
 */

/**
 * 解析器接口（插件式架构）
 * 实现 NFR5 可维护性要求
 */
export interface TestResultParser {
  name: string;                              // 解析器名称（如 'jest', 'vitest'）
  supportedFormats: string[];                // 支持的文件格式（如 ['.json', '.xml']）
  priority: number;                          // 优先级（数字越小优先级越高）
  canParse(filePath: string, content: string): boolean;  // 判断是否能解析
  parse(content: string): ParsedTestResult;  // 解析逻辑
}

/**
 * 解析后的测试结果
 */
export interface ParsedTestResult {
  summary: TestSummary;
  testCases: TestCase[];
  failures: FailureDetail[];
  coverage?: CoverageData;
  metadata: TestMetadata;
}

/**
 * 测试摘要
 */
export interface TestSummary {
  total: number;
  passed: number;
  failed: number;
  skipped: number;
  duration: number;    // 毫秒
  passRate: number;    // 百分比
}

/**
 * 测试用例
 */
export interface TestCase {
  name: string;
  file: string;
  status: 'passed' | 'failed' | 'skipped';
  duration: number;
}

/**
 * 失败详情
 */
export interface FailureDetail {
  testCaseName: string;
  file: string;
  errorMessage: string;
  stackTrace: string;  // 截断至20行
}

/**
 * 覆盖率数据
 */
export interface CoverageData {
  statements: number;
  branches: number;
  functions: number;
  lines: number;
  lowCoverageFiles?: string[];
}

/**
 * 测试元数据
 */
export interface TestMetadata {
  framework: string;
  version?: string;
  timestamp: string;
  command?: string;
}

/**
 * 配置项
 */
export interface TestReportConfig {
  testCommand?: string;      // 测试执行命令
  resultFile?: string;       // 解析模式结果文件路径
  outputFormat: 'markdown' | 'html' | 'json';  // 输出格式
  outputPath: string;        // 报告输出目录
  coverage: 'auto' | 'on' | 'off';  // 覆盖率收集
  failThreshold?: number;    // 通过率阈值（百分比）
}

/**
 * 安全过滤规则
 */
export interface SecurityFilterRule {
  pattern: RegExp;
  replacement: string;
  description: string;
}