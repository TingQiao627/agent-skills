/**
 * Jest/Vitest JSON 解析器
 */

/**
 * 解析 Jest JSON 结果
 */
function parseJestJson(data) {
  try {
    const json = typeof data === 'string' ? JSON.parse(data) : data;
    
    const result = {
      project: 'unknown',
      framework: 'jest',
      frameworkVersion: 'unknown',
      command: 'npm test',
      timestamp: new Date().toISOString(),
      total: 0,
      passed: 0,
      failed: 0,
      skipped: 0,
      duration: 0,
      suites: []
    };
    
    // Jest JSON 格式: { success, testResults: [...] }
    if (json.testResults) {
      for (const testResult of json.testResults) {
        const suite = {
          name: testResult.name || 'unknown',
          file: testResult.name || 'unknown',
          cases: [],
          passed: 0,
          failed: 0,
          skipped: 0,
          duration: testResult.duration || 0
        };
        
        if (testResult.assertionResults) {
          for (const assertion of testResult.assertionResults) {
            const testCase = {
              name: assertion.fullName || assertion.title || 'unknown',
              file: testResult.name || 'unknown',
              status: assertion.status || 'passed',
              duration: assertion.duration || 0,
              error: null,
              stack: null
            };
            
            if (assertion.status === 'failed' && assertion.failureMessages) {
              testCase.error = assertion.failureMessages[0] || 'Unknown error';
            }
            
            suite.cases.push(testCase);
            
            if (testCase.status === 'passed') suite.passed++;
            else if (testCase.status === 'failed') suite.failed++;
            else suite.skipped++;
          }
        }
        
        result.suites.push(suite);
        result.total += suite.passed + suite.failed + suite.skipped;
        result.passed += suite.passed;
        result.failed += suite.failed;
        result.skipped += suite.skipped;
        result.duration += suite.duration;
      }
    }
    
    return result;
  } catch (error) {
    throw new Error(`Failed to parse Jest JSON: ${error.message}`);
  }
}

module.exports = { parseJestJson, parse: parseJestJson };