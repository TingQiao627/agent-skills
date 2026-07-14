/**
 * 测试框架检测器
 */

export interface FrameworkInfo {
  name: string;
  command: string;
  configFiles: string[];
  resultFormat: 'jest-json' | 'vitest-json' | 'junit-xml' | 'pytest-xml';
}

export class FrameworkDetector {
  /**
   * 检测项目使用的测试框架
   */
  detectFramework(projectPath: string): FrameworkInfo | null {
    // 检测优先级列表
    const detectors = [
      this.detectJest,
      this.detectVitest,
      this.detectPytest,
      this.detectCargo
    ];
    
    for (const detector of detectors) {
      const framework = detector(projectPath);
      if (framework) {
        return framework;
      }
    }
    
    return null;
  }
  
  /**
   * 检测 Jest
   */
  private detectJest(projectPath: string): FrameworkInfo | null {
    const configFiles = [
      'jest.config.js',
      'jest.config.ts',
      'jest.config.json',
      'jest.config.mjs'
    ];
    
    // 简化检测逻辑（实际实现需要读取文件系统）
    return {
      name: 'Jest',
      command: 'npm test -- --json --outputFile=test-results.json',
      configFiles,
      resultFormat: 'jest-json'
    };
  }
  
  /**
   * 检测 Vitest
   */
  private detectVitest(projectPath: string): FrameworkInfo | null {
    const configFiles = [
      'vitest.config.ts',
      'vitest.config.js',
      'vite.config.ts',
      'vite.config.js'
    ];
    
    return {
      name: 'Vitest',
      command: 'npm test -- --reporter=json --outputFile=test-results.json',
      configFiles,
      resultFormat: 'vitest-json'
    };
  }
  
  /**
   * 检测 pytest
   */
  private detectPytest(projectPath: string): FrameworkInfo | null {
    const configFiles = [
      'pytest.ini',
      'pyproject.toml',
      'setup.cfg'
    ];
    
    return {
      name: 'pytest',
      command: 'pytest --junitxml=test-results.xml',
      configFiles,
      resultFormat: 'pytest-xml'
    };
  }
  
  /**
   * 检测 Cargo (Rust)
   */
  private detectCargo(projectPath: string): FrameworkInfo | null {
    return {
      name: 'cargo-test',
      command: 'cargo test',
      configFiles: ['Cargo.toml'],
      resultFormat: 'junit-xml'
    };
  }
}