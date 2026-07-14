/**
 * 敏感信息过滤器
 * 
 * 过滤测试报告中的敏感信息：
 * - 环境变量（process.env, os.environ）
 * - 密钥类内容（API_KEY, TOKEN, SECRET）
 * - 敏感路径（/home/{user}, /Users/{user}）
 */

/**
 * 敏感信息正则模式
 */
const SENSITIVE_PATTERNS = [
  // 环境变量引用
  /process\.env\.\w+/g,
  /os\.environ\[['"]\w+['"]]/g,
  
  // 密钥类内容
  /\b(API[_-]?KEY|TOKEN|SECRET|PASSWORD|PRIVATE[_-]?KEY|ACCESS[_-]?KEY)\s*[=:]\s*['"]?[\w\-]+['"]?/gi,
  /\b(api[_-]?key|token|secret|password|private[_-]?key|access[_-]?key)['"]?\s*:\s*['"]?[\w\-]+['"]?/gi,
  
  // Bearer Token
  /Bearer\s+[\w\-\.]+/gi,
  
  // JWT Token
  /eyJ[A-Za-z0-9-_]+\.eyJ[A-Za-z0-9-_]+\.[A-Za-z0-9-_]+/g,
  
  // 敏感路径
  /\/home\/[\w]+/g,
  /\/Users\/[\w]+/g,
  /\\Users\\[\w]+/g,
  /C:\\Users\\[\w]+/g,
  
  // IP 地址（可选）
  /\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b/g
];

/**
 * 替换文本
 */
const REPLACEMENT_TEXT = '[已过滤]';

/**
 * 过滤字符串中的敏感信息
 * @param {string} text - 原始文本
 * @returns {string} 过滤后的文本
 */
function sanitizeText(text) {
  if (!text || typeof text !== 'string') {
    return text;
  }
  
  let result = text;
  
  SENSITIVE_PATTERNS.forEach(pattern => {
    result = result.replace(pattern, REPLACEMENT_TEXT);
  });
  
  return result;
}

/**
 * 过滤 TestResult 对象中的敏感信息
 * @param {TestResult} testResult - 测试结果对象
 * @returns {TestResult} 过滤后的测试结果对象
 */
function sanitize(testResult) {
  if (!testResult) {
    return testResult;
  }
  
  // 深拷贝以避免修改原对象
  const result = JSON.parse(JSON.stringify(testResult));
  
  // 过滤测试文件路径
  if (result.testFiles) {
    result.testFiles.forEach(file => {
      file.path = sanitizeText(file.path);
      
      // 过滤测试用例
      if (file.tests) {
        file.tests.forEach(test => {
          test.name = sanitizeText(test.name);
          
          // 过滤错误信息
          if (test.error) {
            test.error.message = sanitizeText(test.error.message);
            
            if (test.error.stack) {
              test.error.stack = test.error.stack.map(line => sanitizeText(line));
            }
          }
        });
      }
    });
  }
  
  // 过滤覆盖率文件路径
  if (result.coverage && result.coverage.lowCoverageFiles) {
    result.coverage.lowCoverageFiles.forEach(file => {
      file.path = sanitizeText(file.path);
    });
  }
  
  return result;
}

/**
 * 检查文本是否包含敏感信息
 * @param {string} text - 待检查文本
 * @returns {boolean} 是否包含敏感信息
 */
function hasSensitiveInfo(text) {
  if (!text || typeof text !== 'string') {
    return false;
  }
  
  return SENSITIVE_PATTERNS.some(pattern => pattern.test(text));
}

/**
 * 脱敏路径
 * @param {string} filePath - 文件路径
 * @returns {string} 脱敏后的路径
 */
function sanitizePath(filePath) {
  if (!filePath || typeof filePath !== 'string') {
    return filePath;
  }
  
  // 替换用户目录
  return filePath
    .replace(/\/home\/[\w]+/g, '/home/[user]')
    .replace(/\/Users\/[\w]+/g, '/Users/[user]')
    .replace(/\\Users\\[\w]+/g, '\\Users\\[user]')
    .replace(/C:\\Users\\[\w]+/g, 'C:\\Users\\[user]');
}

module.exports = {
  sanitize,
  sanitizeText,
  sanitizePath,
  hasSensitiveInfo,
  SENSITIVE_PATTERNS,
  REPLACEMENT_TEXT
};