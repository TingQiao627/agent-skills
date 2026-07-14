/**
 * 测试结果解析器基类和接口定义
 */

/**
 * 单个测试用例结果
 */
export interface TestCaseResult {
  name: string;
  file: string;
  status: 'passed' | 'failed' | 'skipped' | 'pending';
  duration: number; // 毫秒
  error?: {
    message: string;
    stack?: string;
  };
}

/**
 * 测试文件结果
 */
export interface TestFileResult {
  file: string;
  testCases: TestCaseResult[];
  duration: number;
}

/**
 * 测试结果摘要
 */
export interface TestSummary {
  total: number;
  passed: number;
  failed: number;
  skipped: number;
  pending: number;
  duration: number; // 毫秒
  passRate: number; // 0-100
  success: boolean;
}

/**
 * 覆盖率数据
 */
export interface CoverageData {
  lines: { total: number; covered: number; percentage: number };
  statements: { total: number; covered: number; percentage: number };
  branches: { total: number; covered: number; percentage: number };
  functions: { total: number; covered: number; percentage: number };
  files?: {
    path: string;
    lines: { total: number; covered: number; percentage: number };
    statements: { total: number; covered: number; percentage: number };
    branches: { total: number; covered: number; percentage: number };
    functions: { total: number; covered: number; percentage: number };
  }[];
}

/**
 * 完整的测试结果
 */
export interface ParsedTestResult {
  framework: string;
  version?: string;
  summary: TestSummary;
  files: TestFileResult[];
  coverage?: CoverageData;
  metadata: {
    startTime?: Date;
    endTime?: Date;
    command?: string;
    resultFile?: string;
  };
}

/**
 * 解析器基类
 */
export abstract class BaseParser {
  abstract readonly name: string;
  abstract parse(content: string, filePath?: string): ParsedTestResult;
  
  /**
   * 截断堆栈信息至可读长度
   */
  protected truncateStack(stack?: string, maxLines: number = 10): string | undefined {
    if (!stack) return undefined;
    
    const lines = stack.split('\n');
    if (lines.length <= maxLines) return stack;
    
    return lines.slice(0, maxLines).join('\n') + `\n... (${lines.length - maxLines} more lines)`;
  }
  
  /**
   * 过滤敏感信息
   */
  protected sanitizeMessage(message: string): string {
    // 移除常见敏感信息模式
    return message
      .replace(/[A-Za-z0-9_-]{20,}/g, '[REDACTED]') // 长字符串（可能是 token）
      .replace(/password[=:]\s*\S+/gi, 'password=[REDACTED]')
      .replace(/token[=:]\s*\S+/gi, 'token=[REDACTED]')
      .replace(/key[=:]\s*\S+/gi, 'key=[REDACTED]')
      .replace(/secret[=:]\s*\S+/gi, 'secret=[REDACTED]');
  }
  
  /**
   * 计算通过率
   */
  protected calculatePassRate(passed: number, total: number): number {
    if (total === 0) return 0;
    return Math.round((passed / total) * 100 * 100) / 100; // 保留两位小数
  }
  
  /**
   * 毫秒转可读时间
   */
  protected formatDuration(ms: number): string {
    if (ms < 1000) return `${ms}ms`;
    if (ms < 60000) return `${(ms / 1000).toFixed(2)}s`;
    return `${Math.floor(ms / 60000)}m ${((ms % 60000) / 1000).toFixed(0)}s`;
  }
}

/**
 * 解析器工厂
 */
export class ParserFactory {
  private static parsers: Map<string, BaseParser> = new Map();
  
  static register(parser: BaseParser): void {
    this.parsers.set(parser.name, parser);
  }
  
  static get(name: string): BaseParser | undefined {
    return this.parsers.get(name);
  }
  
  static has(name: string): boolean {
    return this.parsers.has(name);
  }
}