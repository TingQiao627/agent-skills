/**
 * Jest/Vitest JSON 测试结果解析器
 * 
 * 解析 Jest/Vitest 的 JSON reporter 输出，转换为标准 TestResult 结构
 */

const fs = require('fs');
const path = require('path');

/**
 * 解析 Jest/Vitest JSON 结果文件
 * @param {string} resultFile - JSON 结果文件路径
 * @returns {TestResult} 标准测试结果对象
 */
function parse(resultFile) {
  let rawData;
  let result;
  
  // 1. 读取并解析 JSON
  try {
    rawData = fs.readFileSync(resultFile, 'utf-8');
    result = JSON.parse(rawData);
  } catch (error) {
    throw new Error(`解析 JSON 文件失败: ${error.message}\n文件: ${resultFile}`);
  }
  
  // 2. 检测框架类型和版本
  const framework = detectFramework(result);
  const version = extractVersion(result, framework);
  
  // 3. 统计基本数据
  const stats = {
    total: 0,
    passed: 0,
    failed: 0,
    skipped: 0,
    duration: 0
  };
  
  // 4. 提取测试文件和用例
  const testFiles = [];
  
  if (result.testResults && Array.isArray(result.testResults)) {
    result.testResults.forEach(testFile => {
      const fileData = {
        path: testFile.name || '未知文件',
        tests: []
      };
      
      if (testFile.assertionResults && Array.isArray(testFile.assertionResults)) {
        testFile.assertionResults.forEach(assertion => {
          stats.total++;
          
          const testCase = {
            name: assertion.fullName || assertion.title || '未命名用例',
            status: mapStatus(assertion.status),
            duration: assertion.duration || 0,
            error: null
          };
          
          // 统计状态
          switch (testCase.status) {
            case 'passed':
              stats.passed++;
              break;
            case 'failed':
              stats.failed++;
              // 提取失败信息
              if (assertion.failureMessages && assertion.failureMessages.length > 0) {
                testCase.error = {
                  message: extractErrorMessage(assertion.failureMessages),
                  stack: extractStackTrace(assertion.failureMessages)
                };
              }
              break;
            case 'skipped':
              stats.skipped++;
              break;
          }
          
          fileData.tests.push(testCase);
        });
      }
      
      // 累加文件耗时
      stats.duration += testFile.duration || 0;
      
      testFiles.push(fileData);
    });
  }
  
  // 5. 计算通过率
  const passRate = stats.total > 0 
    ? ((stats.passed / stats.total) * 100).toFixed(2) 
    : '0.00';
  
  // 6. 提取覆盖率（如果存在）
  let coverage = null;
  if (result.coverageMap) {
    coverage = extractCoverage(result.coverageMap);
  }
  
  return {
    framework,
    version,
    total: stats.total,
    passed: stats.passed,
    failed: stats.failed,
    skipped: stats.skipped,
    duration: stats.duration,
    passRate: parseFloat(passRate),
    testFiles,
    coverage
  };
}

/**
 * 检测框架类型（Jest 或 Vitest）
 */
function detectFramework(result) {
  // Vitest 通常有特定的字段
  if (result.testResults && result.testResults[0] && result.testResults[0].name && 
      result.testResults[0].name.includes('vitest')) {
    return 'vitest';
  }
  
  // 通过环境信息判断
  if (result.name === 'vitest' || (result.config && result.config.includes('vitest'))) {
    return 'vitest';
  }
  
  return 'jest';
}

/**
 * 提取框架版本
 */
function extractVersion(result, framework) {
  if (result.version) {
    return result.version;
  }
  
  // 尝试从其他字段提取
  if (result.config && result.config.version) {
    return result.config.version;
  }
  
  return '未获取';
}

/**
 * 映射测试状态
 */
function mapStatus(status) {
  const statusMap = {
    'passed': 'passed',
    'failed': 'failed',
    'pending': 'skipped',
    'skipped': 'skipped',
    'todo': 'skipped',
    'disabled': 'skipped'
  };
  
  return statusMap[status] || 'skipped';
}

/**
 * 提取错误信息（截断至可读长度）
 */
function extractErrorMessage(failureMessages) {
  if (!failureMessages || failureMessages.length === 0) {
    return '未获取错误信息';
  }
  
  const firstMessage = failureMessages[0];
  
  // 截断至前 500 字符
  const maxLen = 500;
  if (firstMessage.length > maxLen) {
    return firstMessage.substring(0, maxLen) + '...';
  }
  
  return firstMessage;
}

/**
 * 提取堆栈信息（截断至 5 行）
 */
function extractStackTrace(failureMessages) {
  if (!failureMessages || failureMessages.length === 0) {
    return [];
  }
  
  const message = failureMessages.join('\n');
  const lines = message.split('\n');
  
  // 提取堆栈行（通常包含 "at " 前缀）
  const stackLines = lines
    .filter(line => line.trim().startsWith('at ') || line.includes('.test.') || line.includes('.spec.'))
    .slice(0, 5);
  
  return stackLines.map(line => line.trim());
}

/**
 * 提取覆盖率数据
 */
function extractCoverage(coverageMap) {
  try {
    // Jest 覆盖率格式较为复杂，这里简化处理
    // 实际实现需要根据覆盖率文件路径提取
    return null;
  } catch (error) {
    return null;
  }
}

module.exports = {
  parse,
  detectFramework,
  mapStatus
};