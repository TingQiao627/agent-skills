/**
 * 框架检测器
 */

const fs = require('fs');
const path = require('path');

/**
 * 检测测试框架
 */
function detectFramework(cwd = process.cwd()) {
  // 检查 package.json
  const packageJsonPath = path.join(cwd, 'package.json');
  if (fs.existsSync(packageJsonPath)) {
    try {
      const packageJson = JSON.parse(fs.readFileSync(packageJsonPath, 'utf-8'));
      
      if (packageJson.scripts && packageJson.scripts.test) {
        const testScript = packageJson.scripts.test.toLowerCase();
        
        if (testScript.includes('jest')) {
          return {
            framework: 'jest',
            command: 'npm test -- --reporter=json --json',
            configFile: findConfigFile(cwd, 'jest')
          };
        }
        
        if (testScript.includes('vitest')) {
          return {
            framework: 'vitest',
            command: 'npm test -- --reporter=json',
            configFile: findConfigFile(cwd, 'vitest')
          };
        }
        
        return {
          framework: 'npm',
          command: 'npm test',
          configFile: null
        };
      }
    } catch (e) {}
  }
  
  // 检查 Python 项目
  const pyprojectPath = path.join(cwd, 'pyproject.toml');
  if (fs.existsSync(pyprojectPath)) {
    return {
      framework: 'pytest',
      command: 'pytest --junit-xml=test-results/junit.xml',
      configFile: findConfigFile(cwd, 'pytest')
    };
  }
  
  return null;
}

/**
 * 查找配置文件
 */
function findConfigFile(cwd, framework) {
  const configPatterns = {
    jest: ['jest.config.js', 'jest.config.ts', 'jest.config.json'],
    vitest: ['vitest.config.js', 'vitest.config.ts'],
    pytest: ['pytest.ini', 'pyproject.toml', 'setup.cfg']
  };
  
  const patterns = configPatterns[framework] || [];
  for (const pattern of patterns) {
    const filePath = path.join(cwd, pattern);
    if (fs.existsSync(filePath)) return filePath;
  }
  
  return null;
}

/**
 * 获取框架版本
 */
function getFrameworkVersion(framework, cwd = process.cwd()) {
  try {
    const packageJsonPath = require.resolve(`${framework}/package.json`, { paths: [cwd] });
    const packageJson = JSON.parse(fs.readFileSync(packageJsonPath, 'utf-8'));
    return packageJson.version || 'unknown';
  } catch (e) {
    return 'unknown';
  }
}

module.exports = { detectFramework, findConfigFile, getFrameworkVersion };