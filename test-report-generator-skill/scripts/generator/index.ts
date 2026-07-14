/**
 * 报告生成器模块导出
 */

export { MarkdownGenerator } from './markdown';

import { MarkdownGenerator } from './markdown';
import { TestResult } from '../parser/base';

export type OutputFormat = 'markdown' | 'html' | 'json';

/**
 * 报告生成器
 */
export class ReportGenerator {
  private markdownGenerator = new MarkdownGenerator();
  
  /**
   * 生成报告
   */
  generate(result: TestResult, format: OutputFormat = 'markdown'): string {
    switch (format) {
      case 'markdown':
        return this.markdownGenerator.generate(result);
      case 'json':
        return JSON.stringify(result, null, 2);
      case 'html':
        throw new Error('HTML format not implemented yet (P1)');
      default:
        throw new Error(`Unsupported format: ${format}`);
    }
  }
  
  /**
   * 获取文件扩展名
   */
  getFileExtension(format: OutputFormat): string {
    switch (format) {
      case 'markdown':
        return '.md';
      case 'json':
        return '.json';
      case 'html':
        return '.html';
      default:
        return '.md';
    }
  }
}