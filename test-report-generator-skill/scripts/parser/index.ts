/**
 * 解析器模块导出
 */

export { BaseParser, ParserRegistry, ParseError } from './base';
export type { TestParserPlugin, TestResult, FailureCase, TestCaseGroup, TestCase, CoverageData } from './base';
export { JestParser } from './jest';
export { VitestParser } from './vitest';
export { JUnitParser } from './junit';

import { ParserRegistry } from './base';
import { JestParser } from './jest';
import { VitestParser } from './vitest';
import { JUnitParser } from './junit';

/**
 * 创建默认解析器注册表
 */
export function createDefaultRegistry(): ParserRegistry {
  const registry = new ParserRegistry();
  
  registry.register(new JestParser());
  registry.register(new VitestParser());
  registry.register(new JUnitParser());
  
  return registry;
}