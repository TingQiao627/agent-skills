/**
 * 安全过滤模块
 * 
 * 实现 NFR3 安全要求：
 * - 过滤环境变量泄露
 * - 正则匹配并替换敏感路径
 * - 命令注入防护
 */

import { SecurityFilterRule } from './types';

/**
 * 安全过滤规则表
 */
export const SECURITY_FILTER_RULES: SecurityFilterRule[] = [
  {
    pattern: /process\.env\.[A-Z_]+/g,
    replacement: '[ENV_VAR]',
    description: '环境变量过滤'
  },
  {
    pattern: /(password|secret|token|key|auth|api_key)["']?\s*[:=]\s*["']?[^,}"'\s]+/gi,
    replacement: '[REDACTED]',
    description: '密钥标识过滤'
  },
  {
    pattern: /\/home\/[^/\s]+|\/Users\/[^/\s]+|C:\\Users\\[^\s\\]+/g,
    replacement: '[HOME]',
    description: '敏感路径过滤'
  },
  {
    pattern: /(sk-[a-zA-Z0-9]{20,}|ghp_[a-zA-Z0-9]{36}|xox[baprs]-[a-zA-Z0-9-]+)/g,
    replacement: '[API_KEY]',
    description: 'API Key 过滤'
  },
  {
    pattern: /["']?[\w-]+["']?\s*[:=]\s*["']?[A-Za-z0-9+/]{40,}={0,2}["']?/g,
    replacement: '[SECRET]',
    description: 'Base64 编码密钥过滤'
  }
];

/**
 * Shell 元字符（命令注入防护）
 */
const SHELL_METACHAR_REGEX = /[|;&$`(){}<>]/;

/**
 * 应用安全过滤
 */
export function applySecurityFilters(content: string): string {
  let filtered = content;
  
  for (const rule of SECURITY_FILTER_RULES) {
    filtered = filtered.replace(rule.pattern, rule.replacement);
  }
  
  return filtered;
}

/**
 * 过滤堆栈信息
 */
export function filterStackTrace(stackTrace: string, maxLines: number = 20): string {
  const lines = stackTrace.split('\n');
  
  // 过滤 node_modules 和敏感路径
  const filtered = lines.filter(line => 
    !line.includes('node_modules') &&
    !line.includes('.env') &&
    !line.includes('secrets/')
  );
  
  // 应用安全过滤规则
  const secured = filtered.map(line => applySecurityFilters(line));
  
  // 截断至关键 maxLines 行
  return secured.slice(0, maxLines).join('\n');
}

/**
 * 验证命令参数（命令注入防护）
 */
export function validateCommandSafety(command: string): { safe: boolean; reason?: string } {
  if (SHELL_METACHAR_REGEX.test(command)) {
    return {
      safe: false,
      reason: `命令包含不安全的 shell 元字符，可能存在命令注入风险`
    };
  }
  
  return { safe: true };
}

/**
 * 清洗测试报告内容
 */
export function sanitizeReportContent(content: string): string {
  return applySecurityFilters(content);
}