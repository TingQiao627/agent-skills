/**
 * 解析器注册表
 * 
 * 实现插件式架构（NFR5）
 */

import { TestResultParser } from '../types';
import { JestParser } from './jest-parser';
import { VitestParser } from './vitest-parser';
import { JUnitXmlParser } from './junit-xml-parser';

/**
 * 已注册的解析器列表（按优先级排序）
 */
export const parsers: TestResultParser[] = [
  JestParser,
  VitestParser,
  JUnitXmlParser,
].sort((a, b) => a.priority - b.priority);

/**
 * 查找合适的解析器
 */
export function findParser(filePath: string, content: string): TestResultParser | null {
  for (const parser of parsers) {
    if (parser.canParse(filePath, content)) {
      return parser;
    }
  }
  return null;
}

/**
 * 解析测试结果
 */
export function parseTestResult(filePath: string, content: string): any {
  const parser = findParser(filePath, content);
  
  if (!parser) {
    throw new Error(`No parser found for file: ${filePath}`);
  }
  
  return parser.parse(content);
}