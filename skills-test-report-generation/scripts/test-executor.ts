/**
 * 测试执行器
 * 执行测试命令并收集结果
 */

import { exec } from 'child_process';
import { promisify } from 'util';
import * as fs from 'fs';
import * as path from 'path';
import { DetectedFramework } from './framework-detector';

const execAsync = promisify(exec);

export interface TestExecutionResult {
  success: boolean;
  exitCode: number;
  stdout: string;
  stderr: string;
  resultFile?: string;
  error?: string;
}

export interface ExecutionOptions {
  timeout?: number; // 毫秒
  cwd?: string;
  env?: Record<string, string>;
}

/**
 * 执行测试命令
 */
export async function executeTest(
  detected: DetectedFramework,
  options: ExecutionOptions = {}
): Promise<TestExecutionResult> {
  const cwd = options.cwd || process.cwd();
  const timeout = options.timeout || 300000; // 默认 5 分钟
  
  // 确保结果目录存在
  const reportsDir = path.join(cwd, 'reports');
  if (!fs.existsSync(reportsDir)) {
    fs.mkdirSync(reportsDir, { recursive: true });
  }
  
  try {
    const command = `${detected.command} ${detected.args.join(' ')}`;
    
    const { stdout, stderr } = await execAsync(command, {
      cwd,
      timeout,
      env: { ...process.env, ...options.env },
      maxBuffer: 10 * 1024 * 1024, // 10MB
    });
    
    // 查找结果文件
    const resultFile = findResultFile(detected, cwd);
    
    return {
      success: true,
      exitCode: 0,
      stdout,
      stderr,
      resultFile,
    };
  } catch (error: unknown) {
    const execError = error as { code?: number; stdout?: string; stderr?: string; message?: string };
    
    // 测试失败（非零退出码）也属于正常情况，有结果可解析
    if (execError.stdout !== undefined) {
      const resultFile = findResultFile(detected, cwd);
      return {
        success: false,
        exitCode: execError.code || 1,
        stdout: execError.stdout || '',
        stderr: execError.stderr || '',
        resultFile,
      };
    }
    
    // 执行失败（命令无法运行）
    return {
      success: false,
      exitCode: execError.code || -1,
      stdout: '',
      stderr: execError.message || 'Unknown error',
      error: diagnoseExecutionError(execError),
    };
  }
}

/**
 * 查找测试结果文件
 */
function findResultFile(detected: DetectedFramework, cwd: string): string | undefined {
  const possibleFiles: string[] = [];
  
  switch (detected.framework) {
    case 'jest':
      possibleFiles.push(
        path.join(cwd, 'test-results.json'),
        path.join(cwd, 'reports', 'test-results.json'),
        path.join(cwd, 'test-report.json')
      );
      break;
    case 'vitest':
      possibleFiles.push(
        path.join(cwd, 'test-results.json'),
        path.join(cwd, '.vitest', 'test-results.json'),
        path.join(cwd, 'reports', 'test-results.json')
      );
      break;
    case 'pytest':
      possibleFiles.push(
        path.join(cwd, 'test-results.xml'),
        path.join(cwd, 'test-report.xml'),
        path.join(cwd, 'reports', 'test-results.xml'),
        path.join(cwd, 'junit.xml')
      );
      break;
    default:
      // 通用 JUnit XML
      possibleFiles.push(
        path.join(cwd, 'test-results.xml'),
        path.join(cwd, 'junit.xml'),
        path.join(cwd, 'test-report.xml')
      );
  }
  
  for (const file of possibleFiles) {
    if (fs.existsSync(file)) {
      return file;
    }
  }
  
  return undefined;
}

/**
 * 诊断执行错误
 */
function diagnoseExecutionError(error: { message?: string; code?: number }): string {
  const message = error.message || '';
  
  if (message.includes('ENOENT')) {
    return '命令不存在：请确认测试框架已安装，或使用 result_file 参数指定已有结果文件';
  }
  
  if (message.includes('ETIMEDOUT') || message.includes('timeout')) {
    return '测试执行超时：建议检查是否有长时间运行的测试用例，或使用后台执行模式';
  }
  
  if (message.includes('ENOMEM') || message.includes('memory')) {
    return '内存不足：测试进程可能需要更多内存';
  }
  
  return `测试执行失败: ${message}`;
}

/**
 * 读取已有结果文件（解析模式）
 */
export function readResultFile(filePath: string): { success: boolean; content?: string; error?: string } {
  try {
    if (!fs.existsSync(filePath)) {
      return { success: false, error: `结果文件不存在: ${filePath}` };
    }
    
    const content = fs.readFileSync(filePath, 'utf-8');
    
    if (!content.trim()) {
      return { success: false, error: '结果文件为空' };
    }
    
    return { success: true, content };
  } catch (error: unknown) {
    return {
      success: false,
      error: `读取结果文件失败: ${(error as Error).message}`,
    };
  }
}

/**
 * 检测结果文件格式
 */
export function detectResultFormat(content: string, filePath: string): 'jest-json' | 'vitest-json' | 'junit-xml' | 'pytest-json' | 'unknown' {
  const ext = path.extname(filePath).toLowerCase();
  const trimmed = content.trim();
  
  // JSON 格式
  if (ext === '.json' || trimmed.startsWith('{')) {
    try {
      const json = JSON.parse(trimmed);
      
      // Jest 特征
      if (json.success !== undefined || json.numTotalTests !== undefined) {
        return 'jest-json';
      }
      
      // Vitest 特征
      if (json.testResults !== undefined && Array.isArray(json.testResults)) {
        return 'vitest-json';
      }
      
      // pytest-json
      if (json.tests !== undefined && json.summary !== undefined) {
        return 'pytest-json';
      }
    } catch {
      // 不是有效 JSON
    }
  }
  
  // XML 格式 (JUnit)
  if (ext === '.xml' || trimmed.startsWith('<?xml') || trimmed.startsWith('<testsuite')) {
    return 'junit-xml';
  }
  
  return 'unknown';
}