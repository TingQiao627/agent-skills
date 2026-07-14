/**
 * 框架检测模块
 * 
 * 实现 FR1.1 自动识别测试框架
 * 支持三层识别优先级
 */

import * as fs from 'fs';
import * as path from 'path';

export interface DetectedFramework {
  name: 'jest' | 'vitest' | 'pytest' | 'junit-xml';
  configPath?: string;
  testCommand: string;
  coverageCommand?: string;
}

/**
 * 检测项目使用的测试框架
 */
export function detectTestFramework(projectRoot: string): DetectedFramework | null {
  // 优先级1: 框架特征文件
  const frameworkChecks = [
    detectJest,
    detectVitest,
    detectPytest,
  ];
  
  for (const check of frameworkChecks) {
    const detected = check(projectRoot);
    if (detected) {
      return detected;
    }
  }
  
  // 优先级2: 项目配置文件
  const configDetected = detectFromConfig(projectRoot);
  if (configDetected) {
    return configDetected;
  }
  
  return null;
}

/**
 * 检测 Jest
 */
function detectJest(projectRoot: string): DetectedFramework | null {
  const configFiles = [
    'jest.config.js',
    'jest.config.ts',
    'jest.config.json',
  ];
  
  for (const configFile of configFiles) {
    const configPath = path.join(projectRoot, configFile);
    if (fs.existsSync(configPath)) {
      return {
        name: 'jest',
        configPath,
        testCommand: 'npx jest --json --outputFile=jest-output.json --testLocationInResults --coverage',
        coverageCommand: 'npx jest --coverage --coverageReporters=json',
      };
    }
  }
  
  return null;
}

/**
 * 检测 Vitest
 */
function detectVitest(projectRoot: string): DetectedFramework | null {
  const configFiles = [
    'vitest.config.ts',
    'vite.config.ts',
  ];
  
  for (const configFile of configFiles) {
    const configPath = path.join(projectRoot, configFile);
    if (fs.existsSync(configPath)) {
      return {
        name: 'vitest',
        configPath,
        testCommand: 'npx vitest run --reporter=json --outputFile=vitest-output.json --coverage',
        coverageCommand: 'npx vitest run --coverage',
      };
    }
  }
  
  return null;
}

/**
 * 检测 pytest
 */
function detectPytest(projectRoot: string): DetectedFramework | null {
  const configFiles = [
    'pytest.ini',
    'pyproject.toml',
    'setup.cfg',
  ];
  
  for (const configFile of configFiles) {
    const configPath = path.join(projectRoot, configFile);
    if (fs.existsSync(configPath)) {
      return {
        name: 'pytest',
        configPath,
        testCommand: 'pytest --junit-xml=pytest-results.xml',
        coverageCommand: 'pytest --cov --cov-report=json',
      };
    }
  }
  
  return null;
}

/**
 * 从项目配置文件检测
 */
function detectFromConfig(projectRoot: string): DetectedFramework | null {
  // package.json
  const packageJsonPath = path.join(projectRoot, 'package.json');
  if (fs.existsSync(packageJsonPath)) {
    try {
      const packageJson = JSON.parse(fs.readFileSync(packageJsonPath, 'utf-8'));
      if (packageJson.scripts?.test) {
        // 根据 test 脚本推断框架
        const testScript = packageJson.scripts.test;
        if (testScript.includes('jest')) {
          return {
            name: 'jest',
            testCommand: 'npm test -- --json --outputFile=jest-output.json --testLocationInResults',
          };
        }
        if (testScript.includes('vitest')) {
          return {
            name: 'vitest',
            testCommand: 'npm test -- --reporter=json --outputFile=vitest-output.json',
          };
        }
      }
    } catch {
      // JSON 解析失败，忽略
    }
  }
  
  // pyproject.toml
  const pyprojectPath = path.join(projectRoot, 'pyproject.toml');
  if (fs.existsSync(pyprojectPath)) {
    return {
      name: 'pytest',
      testCommand: 'pytest --junit-xml=pytest-results.xml',
    };
  }
  
  return null;
}

/**
 * 尝试通用测试命令（降级策略）
 */
export function getFallbackCommand(): string[] {
  return [
    'npm test',
    'npx jest',
    'npx vitest run',
    'pytest',
    'cargo test',
  ];
}