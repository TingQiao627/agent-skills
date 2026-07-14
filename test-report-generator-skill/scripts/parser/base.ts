/**
 * 测试结果解析器基类
 * 定义统一的解析器接口
 */

export interface TestResult {
  metadata: {
    projectName: string;
    generatedAt: string;
    testCommand: string;
    framework: string;
    frameworkVersion?: string;
    environment: string;
  };
  summary: {
    total: number;
    passed: number;
    failed: number;
    skipped: number;
    duration: number;
    passRate: number;
    status: 'passed' | 'failed';
  };
  failures: FailureCase[];
  testCases: TestCaseGroup[];
  coverage?: CoverageData;
  appendix: {
    resultFilePath: string;
    generatorVersion: string;
  };
}

export interface FailureCase {
  name: string;
  file: string;
  error: {
    message: string;
    stack?: string;
  };
  duration?: number;
}

export interface TestCaseGroup {
  file: string;
  cases: TestCase[];
  duration: number;
}

export interface TestCase {
  name: string;
  status: 'passed' | 'failed' | 'skipped';
  duration: number;
}

export interface CoverageData {
  summary: {
    statements: number;
    branches: number;
    functions: number;
    lines: number;
  };
  lowCoverageFiles?: Array<{
    file: string;
    coverage: {
      statements: number;
      branches: number;
      functions: number;
      lines: number;
    };
  }>;
}

/**
 * 解析器插件接口
 */
export interface TestParserPlugin {
  name: string;
  supportedFormats: string[];
  
  canParse(filePath: string, content?: string): boolean;
  parse(filePath: string, content: string): Promise<TestResult>;
}

/**
 * 解析器基类
 */
export abstract class BaseParser implements TestParserPlugin {
  abstract name: string;
  abstract supportedFormats: string[];
  
  abstract canParse(filePath: string, content?: string): boolean;
  abstract parse(filePath: string, content: string): Promise<TestResult>;
  
  /**
   * 截断堆栈信息
   */
  protected truncateStack(stack: string, maxLines: number = 10): string {
    const lines = stack.split('\n').slice(0, maxLines);
    return lines.join('\n');
  }
  
  /**
   * 过滤敏感信息
   */
  protected sanitizeMessage(message: string): string {
    // 过滤环境变量
    message = message.replace(/(?:API_KEY|SECRET|PASSWORD|TOKEN)\s*[=:]\s*\S+/gi, '[REDACTED]');
    
    // 过滤绝对路径中的用户目录
    message = message.replace(/\/Users\/[^/]+\/|\/home\/[^/]+\/|C:\\Users\\[^\\]+\\/g, '~/');
    
    return message;
  }
  
  /**
   * 计算通过率
   */
  protected calculatePassRate(passed: number, total: number): number {
    if (total === 0) return 0;
    return Math.round((passed / total) * 1000) / 10;
  }
}

/**
 * 解析器注册表
 */
export class ParserRegistry {
  private plugins: TestParserPlugin[] = [];
  
  register(plugin: TestParserPlugin): void {
    this.plugins.push(plugin);
  }
  
  findParser(filePath: string, content?: string): TestParserPlugin | null {
    return this.plugins.find(p => p.canParse(filePath, content)) || null;
  }
  
  getSupportedFormats(): string[] {
    return this.plugins.flatMap(p => p.supportedFormats);
  }
}

/**
 * 解析错误类
 */
export class ParseError extends Error {
  constructor(
    public code: string,
    message: string,
    public details?: string
  ) {
    super(message);
    this.name = 'ParseError';
  }
}