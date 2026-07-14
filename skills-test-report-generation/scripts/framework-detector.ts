/**
 * 测试框架检测器
 * 自动检测项目使用的测试框架与运行命令
 */

import * as fs from 'fs';
import * as path from 'path';

export type TestFramework = 'jest' | 'vitest' | 'pytest' | 'junit' | 'unknown';

export interface DetectedFramework {
  framework: TestFramework;
  command: string;
  args: string[];
  configFile?: string;
  version?: string;
}

export interface DetectionOptions {
  explicitCommand?: string;
  cwd?: string;
}

/**
 * 检测测试框架（按优先级）
 */
export function detectTestFramework(options: DetectionOptions = {}): DetectedFramework | null {
  const cwd = options.cwd || process.cwd();
  
  // 优先级 1: 用户显式指定
  if (options.explicitCommand) {
    return parseExplicitCommand(options.explicitCommand);
  }
  
  // 优先级 2: 项目配置文件
  const fromConfig = detectFromConfig(cwd);
  if (fromConfig) return fromConfig;
  
  // 优先级 3: 框架特征文件
  const fromFeatureFiles = detectFromFeatureFiles(cwd);
  if (fromFeatureFiles) return fromFeatureFiles;
  
  return null;
}

/**
 * 解析用户显式指定的命令
 */
function parseExplicitCommand(command: string): DetectedFramework {
  const parts = command.split(' ');
  const cmd = parts[0];
  const args = parts.slice(1);
  
  // 根据命令推断框架
  let framework: TestFramework = 'unknown';
  if (cmd.includes('jest') || args.some(a => a.includes('jest'))) {
    framework = 'jest';
  } else if (cmd.includes('vitest') || args.some(a => a.includes('vitest'))) {
    framework = 'vitest';
  } else if (cmd === 'pytest' || cmd === 'python' && args[0] === '-m' && args[1] === 'pytest') {
    framework = 'pytest';
  }
  
  return {
    framework,
    command: cmd,
    args,
  };
}

/**
 * 从项目配置文件检测
 */
function detectFromConfig(cwd: string): DetectedFramework | null {
  // package.json (Node.js)
  const packageJsonPath = path.join(cwd, 'package.json');
  if (fs.existsSync(packageJsonPath)) {
    try {
      const packageJson = JSON.parse(fs.readFileSync(packageJsonPath, 'utf-8'));
      const scripts = packageJson.scripts || {};
      
      // 检查 test script
      if (scripts.test) {
        return detectFromTestScript(scripts.test, cwd);
      }
      
      // 检查依赖推断框架
      const deps = { ...packageJson.dependencies, ...packageJson.devDependencies };
      if (deps.jest) {
        return { framework: 'jest', command: 'npm', args: ['test', '--', '--json'] };
      }
      if (deps.vitest) {
        return { framework: 'vitest', command: 'npm', args: ['test', '--', '--reporter=json'] };
      }
    } catch {
      // JSON 解析失败，继续尝试其他方式
    }
  }
  
  // pyproject.toml (Python)
  const pyprojectPath = path.join(cwd, 'pyproject.toml');
  if (fs.existsSync(pyprojectPath)) {
    return { framework: 'pytest', command: 'pytest', args: ['--junitxml=test-results.xml'] };
  }
  
  // Cargo.toml (Rust)
  const cargoPath = path.join(cwd, 'Cargo.toml');
  if (fs.existsSync(cargoPath)) {
    return { framework: 'unknown', command: 'cargo', args: ['test'] };
  }
  
  return null;
}

/**
 * 从 test script 推断框架
 */
function detectFromTestScript(testScript: string, cwd: string): DetectedFramework | null {
  if (testScript.includes('jest')) {
    return { framework: 'jest', command: 'npm', args: ['test', '--', '--json', '--outputFile=test-results.json'] };
  }
  if (testScript.includes('vitest')) {
    return { framework: 'vitest', command: 'npm', args: ['test', '--', '--reporter=json', '--outputFile=test-results.json'] };
  }
  if (testScript.includes('pytest')) {
    return { framework: 'pytest', command: 'pytest', args: ['--junitxml=test-results.xml'] };
  }
  
  // 默认使用 npm test
  return { framework: 'unknown', command: 'npm', args: ['test'] };
}

/**
 * 从框架特征文件检测
 */
function detectFromFeatureFiles(cwd: string): DetectedFramework | null {
  // Jest 配置文件
  const jestConfigs = ['jest.config.js', 'jest.config.ts', 'jest.config.json'];
  for (const config of jestConfigs) {
    if (fs.existsSync(path.join(cwd, config))) {
      return { framework: 'jest', command: 'npm', args: ['test', '--', '--json', '--outputFile=test-results.json'], configFile: config };
    }
  }
  
  // Vitest 配置文件
  const vitestConfigs = ['vitest.config.js', 'vitest.config.ts'];
  for (const config of vitestConfigs) {
    if (fs.existsSync(path.join(cwd, config))) {
      return { framework: 'vitest', command: 'npm', args: ['test', '--', '--reporter=json', '--outputFile=test-results.json'], configFile: config };
    }
  }
  
  // pytest 配置文件
  const pytestConfigs = ['pytest.ini', 'setup.cfg', 'tox.ini'];
  for (const config of pytestConfigs) {
    if (fs.existsSync(path.join(cwd, config))) {
      return { framework: 'pytest', command: 'pytest', args: ['--junitxml=test-results.xml'], configFile: config };
    }
  }
  
  return null;
}

/**
 * 构建完整的测试执行命令
 */
export function buildTestCommand(detected: DetectedFramework, mode: 'execution' | 'parse'): { command: string; args: string[] } | null {
  if (mode === 'parse') {
    return null; // 解析模式不需要执行命令
  }
  
  return {
    command: detected.command,
    args: detected.args,
  };
}