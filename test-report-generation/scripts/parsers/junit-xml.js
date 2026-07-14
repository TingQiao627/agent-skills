/**
 * JUnit XML 测试结果解析器
 * 
 * 解析 JUnit XML 格式的测试结果（pytest、通用 JUnit）
 * 支持 pytest --junit-xml 输出及其他 JUnit 兼容格式
 */

const fs = require('fs');
const path = require('path');

/**
 * 解析 JUnit XML 结果文件
 * @param {string} resultFile - XML 结果文件路径
 * @returns {TestResult} 标准测试结果对象
 */
function parse(resultFile) {
  let xmlData;
  
  // 1. 读取 XML 文件
  try {
    xmlData = fs.readFileSync(resultFile, 'utf-8');
  } catch (error) {
    throw new Error(`读取 XML 文件失败: ${error.message}\n文件: ${resultFile}`);
  }
  
  // 2. 简单 XML 解析（不依赖外部库）
  const result = parseJUnitXML(xmlData);
  
  return result;
}

/**
 * 简单 JUnit XML 解析器
 * 不依赖 xml2js 等库，使用正则表达式解析
 */
function parseJUnitXML(xml) {
  // 提取 testsuites 或 testsuite 根节点
  const testsuitesMatch = xml.match(/<testsuites[^>]*>/);
  const isMultiSuite = testsuitesMatch !== null;
  
  // 统计数据
  const stats = {
    total: 0,
    passed: 0,
    failed: 0,
    skipped: 0,
    duration: 0
  };
  
  // 从根节点提取属性
  if (isMultiSuite) {
    const rootMatch = xml.match(/<testsuites[^>]*tests="(\d+)"[^>]*failures="(\d+)"[^>]*skipped="(\d+)"[^>]*>/);
    if (rootMatch) {
      stats.total = parseInt(rootMatch[1]) || 0;
      stats.failed = parseInt(rootMatch[2]) || 0;
      stats.skipped = parseInt(rootMatch[3]) || 0;
      stats.passed = stats.total - stats.failed - stats.skipped;
    }
  }
  
  // 提取所有 testsuite
  const testsuites = [];
  const testsuiteRegex = /<testsuite[^>]*>([\s\S]*?)<\/testsuite>/g;
  let match;
  
  while ((match = testsuiteRegex.exec(xml)) !== null) {
    const testsuiteContent = match[1];
    const testsuiteAttrs = parseAttributes(match[0].match(/<testsuite[^>]*>/)[0]);
    
    const testFile = {
      path: testsuiteAttrs.name || '未知测试套件',
      tests: []
    };
    
    // 提取所有 testcase
    const testcaseRegex = /<testcase[^>]*>([\s\S]*?)<\/testcase>/g;
    let tcMatch;
    
    while ((tcMatch = testcaseRegex.exec(testsuiteContent)) !== null) {
      const testcaseContent = tcMatch[1];
      const testcaseAttrs = parseAttributes(tcMatch[0].match(/<testcase[^>]*>/)[0]);
      
      const testCase = {
        name: testcaseAttrs.name || '未命名用例',
        status: 'passed',
        duration: parseFloat(testcaseAttrs.time) * 1000 || 0, // 秒转毫秒
        error: null
      };
      
      // 检查是否有失败
      if (testcaseContent.includes('<failure')) {
        testCase.status = 'failed';
        
        // 提取失败信息
        const failureMatch = testcaseContent.match(/<failure[^>]*>([\s\S]*?)<\/failure>/);
        if (failureMatch) {
          const failureAttrs = parseAttributes(testcaseContent.match(/<failure[^>]*>/)[0]);
          testCase.error = {
            message: failureAttrs.message || extractFailureMessage(failureMatch[1]),
            stack: extractStackTrace(failureMatch[1])
          };
        }
      } else if (testcaseContent.includes('<skipped')) {
        testCase.status = 'skipped';
      }
      
      testFile.tests.push(testCase);
    }
    
    // 如果根节点没有统计数据，从 testsuite 计算
    if (!isMultiSuite || stats.total === 0) {
      testFile.tests.forEach(tc => {
        stats.total++;
        if (tc.status === 'passed') stats.passed++;
        else if (tc.status === 'failed') stats.failed++;
        else if (tc.status === 'skipped') stats.skipped++;
      });
    }
    
    testsuites.push(testFile);
  }
  
  // 计算通过率
  const passRate = stats.total > 0 
    ? ((stats.passed / stats.total) * 100).toFixed(2) 
    : '0.00';
  
  // 从根节点提取耗时
  const timeMatch = xml.match(/time="([\d.]+)"/);
  if (timeMatch) {
    stats.duration = parseFloat(timeMatch[1]) * 1000; // 秒转毫秒
  }
  
  return {
    framework: 'junit',
    version: '未获取',
    total: stats.total,
    passed: stats.passed,
    failed: stats.failed,
    skipped: stats.skipped,
    duration: stats.duration,
    passRate: parseFloat(passRate),
    testFiles: testsuites,
    coverage: null // JUnit XML 通常不含覆盖率
  };
}

/**
 * 解析 XML 标签属性
 */
function parseAttributes(tagString) {
  const attrs = {};
  const attrRegex = /(\w+)="([^"]*)"/g;
  let match;
  
  while ((match = attrRegex.exec(tagString)) !== null) {
    attrs[match[1]] = match[2];
  }
  
  return attrs;
}

/**
 * 提取失败消息（截断至可读长度）
 */
function extractFailureMessage(content) {
  if (!content) return '未获取错误信息';
  
  // 移除 CDATA 标记
  let message = content.replace(/<!\[CDATA\[|\]\]>/g, '').trim();
  
  // 截断至前 500 字符
  const maxLen = 500;
  if (message.length > maxLen) {
    message = message.substring(0, maxLen) + '...';
  }
  
  return message;
}

/**
 * 提取堆栈信息（截断至 5 行）
 */
function extractStackTrace(content) {
  if (!content) return [];
  
  // 移除 CDATA 标记
  const cleanContent = content.replace(/<!\[CDATA\[|\]\]>/g, '');
  
  const lines = cleanContent.split('\n');
  
  // 提取堆栈行（通常包含文件路径）
  const stackLines = lines
    .filter(line => line.includes('.py:') || line.includes('File ') || line.includes('at '))
    .slice(0, 5);
  
  return stackLines.map(line => line.trim());
}

module.exports = {
  parse,
  parseJUnitXML,
  parseAttributes
};