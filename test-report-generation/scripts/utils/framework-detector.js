/**
 * 框架自动检测工具
 * 
 * 根据项目配置文件和框架特征文件自动检测测试框架
 */

const fs = require('fs');
const path = require('path');

/**
 * 检测项目使用的测试框架
 * @param {string} projectRoot - 项目根目录
 * @param {string} testCommand - 用户指定的测试命令（可选）
 * @returns {Object} { framework: string, command: string }
 */
function detect(projectRoot = process.cwd(), testCommand = null) {
  // 优先级 1: 用户显式指定命令
  if (testCommand) {
    return {
      framework: detectFromCommand(testCommand),
      command: testCommand
    };
  }
  
  // 优先级 2: 项目配置文件检测
  const packageJson = readPackageJson(projectRoot);
  if (packageJson && packageJson.scripts && packageJson.scripts.test) {
    return {
      framework: detectFromCommand(packageJson.scripts.test),
      command: packageJson.scripts.test
    };
  }
  
  // 优先级 3: 框架特征文件推断
  const frameworkFromFile = detectFromConfigFiles(projectRoot);
  if (frameworkFromFile) {
    return {
      framework: frameworkFromFile,
      command: getDefaultCommand(frameworkFromFile)
    };
  }
  
  // 默认：JUnit XML 兜底
  return {
    framework: 'junit',
    command: null
  };
}

/**
 * 从命令推断框架
 */
function detectFromCommand(command) {
  if (!command) return 'junit';
  
  const cmd = command.toLowerCase();
  
  if (cmd.includes('jest')) {
    return 'jest';
  }
  
  if (cmd.includes('vitest')) {
    return 'vitest';
  }
  
  if (cmd.includes('pytest')) {
    return 'pytest';
  }
  
  if (cmd.includes('npm test') || cmd.includes('yarn test') || cmd.includes('pnpm test')) {
    // 需要进一步检测
    return detectFromPackageJson();
  }
  
  return 'junit';
}

/**
 * 从配置文件推断框架
 */
function detectFromConfigFiles(projectRoot) {
  const configFiles = [
    { pattern: 'jest.config.*', framework: 'jest' },
    { pattern: 'vitest.config.*', framework: 'vitest' },
    { pattern: 'pytest.ini', framework: 'pytest' },
    { pattern: 'setup.cfg', framework: 'pytest' },
    { pattern: 'pyproject.toml', framework: 'pytest' }
  ];
  
  for (const config of configFiles) {
    const matches = findMatchingFiles(projectRoot, config.pattern);
    if (matches.length > 0) {
      return config.framework;
    }
  }
  
  return null;
}

/**
 * 从 package.json 检测框架
 */
function detectFromPackageJson() {
  const packageJson = readPackageJson(process.cwd());
  
  if (!packageJson || !packageJson.devDependencies) {
    return 'junit';
  }
  
  const deps = packageJson.devDependencies;
  
  if (deps.jest) return 'jest';
  if (deps.vitest) return 'vitest';
  
  return 'junit';
}

/**
 * 获取框架默认测试命令
 */
function getDefaultCommand(framework) {
  const commands = {
    'jest': 'npm test',
    'vitest': 'vitest run',
    'pytest': 'pytest',
    'junit': null
  };
  
  return commands[framework] || null;
}

/**
 * 读取 package.json
 */
function readPackageJson(projectRoot) {
  const packageJsonPath = path.join(projectRoot, 'package.json');
  
  if (!fs.existsSync(packageJsonPath)) {
    return null;
  }
  
  try {
    const content = fs.readFileSync(packageJsonPath, 'utf-8');
    return JSON.parse(content);
  } catch (error) {
    return null;
  }
}

/**
 * 查找匹配的配置文件
 */
function findMatchingFiles(projectRoot, pattern) {
  const files = fs.readdirSync(projectRoot);
  const regex = new RegExp('^' + pattern.replace('*', '.*') + '$');
  
  return files.filter(file => regex.test(file));
}

/**
 * 获取测试执行命令（含结果输出参数）
 */
function getTestCommandWithOutput(framework, outputFile) {
  const commands = {
    'jest': `npm test -- --json --outputFile=${outputFile}`,
    'vitest': `vitest run --reporter=json --outputFile=${outputFile}`,
    'pytest': `pytest --junit-xml=${outputFile}`,
    'junit': null
  };
  
  return commands[framework] || null;
}

/**
 * 检测结果文件格式
 */
function detectResultFormat(resultFile) {
  if (!resultFile) return null;
  
  const ext = path.extname(resultFile).toLowerCase();
  
  if (ext === '.json') {
    return 'json';
  }
  
  if (ext === '.xml') {
    return 'junit-xml';
  }
  
  return null;
}

module.exports = {
  detect,
  detectFromCommand,
  detectFromConfigFiles,
  getDefaultCommand,
  getTestCommandWithOutput,
  detectResultFormat
};