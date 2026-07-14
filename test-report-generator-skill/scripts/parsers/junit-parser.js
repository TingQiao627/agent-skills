/**
 * JUnit XML 解析器
 */

/**
 * 解析 JUnit XML
 */
function parseJUnitXml(xmlContent) {
  try {
    // 简单的 XML 解析（无外部依赖）
    const result = {
      project: 'unknown',
      framework: 'junit',
      frameworkVersion: 'unknown',
      command: 'unknown',
      timestamp: new Date().toISOString(),
      total: 0,
      passed: 0,
      failed: 0,
      skipped: 0,
      duration: 0,
      suites: []
    };
    
    // 提取 testsuites
    const testsuiteMatches = xmlContent.match(/<testsuite[^>]*>/g) || [];
    
    for (const testsuiteMatch of testsuiteMatches) {
      const suite = {
        name: extractAttribute(testsuiteMatch, 'name') || 'unknown',
        file: extractAttribute(testsuiteMatch, 'file') || 'unknown',
        cases: [],
        passed: 0,
        failed: 0,
        skipped: 0,
        duration: parseInt(extractAttribute(testsuiteMatch, 'time') || '0') * 1000
      };
      
      // 提取测试用例
      const testcaseRegex = /<testcase[^>]*name="([^"]*)"[^>]*>/g;
      let match;
      
      while ((match = testcaseRegex.exec(xmlContent)) !== null) {
        const caseName = match[1];
        const caseBlock = extractCaseBlock(xmlContent, match.index);
        
        const testCase = {
          name: caseName,
          file: suite.file,
          status: 'passed',
          duration: parseInt(extractAttribute(match[0], 'time') || '0') * 1000,
          error: null,
          stack: null
        };
        
        // 检查失败
        if (caseBlock.includes('<failure') || caseBlock.includes('<error')) {
          testCase.status = 'failed';
          testCase.error = extractError(caseBlock);
          testCase.stack = extractStackTrace(caseBlock);
          suite.failed++;
        } else if (caseBlock.includes('<skipped')) {
          testCase.status = 'skipped';
          suite.skipped++;
        } else {
          suite.passed++;
        }
        
        suite.cases.push(testCase);
      }
      
      result.suites.push(suite);
      result.total += suite.passed + suite.failed + suite.skipped;
      result.passed += suite.passed;
      result.failed += suite.failed;
      result.skipped += suite.skipped;
      result.duration += suite.duration;
    }
    
    return result;
  } catch (error) {
    throw new Error(`Failed to parse JUnit XML: ${error.message}`);
  }
}

function extractAttribute(xml, name) {
  const match = xml.match(new RegExp(`${name}="([^"]*)"`));
  return match ? match[1] : null;
}

function extractCaseBlock(xml, startIndex) {
  const endIndex = xml.indexOf('</testcase>', startIndex);
  return xml.substring(startIndex, endIndex !== -1 ? endIndex + 11 : xml.length);
}

function extractError(caseBlock) {
  const match = caseBlock.match(/<(?:failure|error)[^>]*message="([^"]*)"/);
  return match ? match[1] : 'Unknown error';
}

function extractStackTrace(caseBlock) {
  const match = caseBlock.match(/<(?:failure|error)[^>]*>([\s\S]*?)<\/(?:failure|error)>/);
  return match ? match[1].trim().substring(0, 500) : null;
}

module.exports = { parseJUnitXml, parse: parseJUnitXml };