/**
 * 测试报告生成器 - 类型定义
 */

/**
 * @typedef {Object} TestCase
 * @property {string} name - 用例名称
 * @property {string} file - 所属文件路径
 * @property {'passed'|'failed'|'skipped'} status - 执行状态
 * @property {number} duration - 耗时（毫秒）
 * @property {string} [error] - 错误信息
 * @property {string} [stack] - 堆栈摘要
 */

/**
 * @typedef {Object} TestSuite
 * @property {string} name - 套件名称
 * @property {string} file - 文件路径
 * @property {TestCase[]} cases - 用例列表
 * @property {number} passed - 通过数
 * @property {number} failed - 失败数
 * @property {number} skipped - 跳过数
 * @property {number} duration - 总耗时（毫秒）
 */

/**
 * @typedef {Object} CoverageData
 * @property {number} statements - 语句覆盖率
 * @property {number} branches - 分支覆盖率
 * @property {number} functions - 函数覆盖率
 * @property {number} lines - 行覆盖率
 * @property {CoverageFile[]} [lowFiles] - 低于阈值的文件
 */

/**
 * @typedef {Object} TestResult
 * @property {string} project - 项目名称
 * @property {string} framework - 框架名称
 * @property {string} frameworkVersion - 框架版本
 * @property {string} command - 执行命令
 * @property {string} timestamp - 时间戳
 * @property {number} total - 总用例数
 * @property {number} passed - 通过数
 * @property {number} failed - 失败数
 * @property {number} skipped - 跳过数
 * @property {number} duration - 总耗时
 * @property {TestSuite[]} suites - 测试套件列表
 * @property {CoverageData} [coverage] - 覆盖率数据
 * @property {string} [resultFile] - 原始结果文件路径
 */

module.exports = {};