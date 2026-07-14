#!/usr/bin/env node
/**
 * 验证脚本 - 测试解析器和报告生成器
 */

const path = require('path');
const fs = require('fs');

// 加载模块
const { parseJestJson } = require('./scripts/parsers/jest-parser');
const { parseJUnitXml } = require('./scripts/parsers/junit-parser');
const { generateMarkdownReport } = require('./scripts/report-generator');

console.log('🧪 测试报告生成器验证\n');

// 测试 Jest 解析器
console.log('1️⃣ 测试 Jest JSON 解析器...');
try {
  const jestData = JSON.parse(fs.readFileSync('./test-data/jest-sample.json', 'utf-8'));
  const jestResult = parseJestJson(jestData);
  
  console.log(`✅ 解析成功`);
  console.log(`   - 总用例: ${jestResult.total}`);
  console.log(`   - 通过: ${jestResult.passed}`);
  console.log(`   - 失败: ${jestResult.failed}`);
  console.log(`   - 跳过: ${jestResult.skipped}`);
  console.log('');
} catch (error) {
  console.log(`❌ Jest 解析失败: ${error.message}\n`);
  process.exit(1);
}

// 测试 JUnit XML 解析器
console.log('2️⃣ 测试 JUnit XML 解析器...');
try {
  const junitXml = `<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="test-suite" tests="3" failures="1" errors="0" time="0.5">
  <testcase name="test1" classname="TestClass" time="0.1"/>
  <testcase name="test2" classname="TestClass" time="0.2">
    <failure message="Assertion failed">Expected true but got false</failure>
  </testcase>
  <testcase name="test3" classname="TestClass" time="0.05">
    <skipped message="Not implemented"/>
  </testcase>
</testsuite>`;
  
  const junitResult = parseJUnitXml(junitXml);
  console.log(`✅ 解析成功`);
  console.log(`   - 总用例: ${junitResult.total}`);
  console.log(`   - 通过: ${junitResult.passed}`);
  console.log(`   - 失败: ${junitResult.failed}`);
  console.log('');
} catch (error) {
  console.log(`❌ JUnit 解析失败: ${error.message}\n`);
  process.exit(1);
}

// 测试报告生成
console.log('3️⃣ 测试 Markdown 报告生成...');
try {
  const jestData = JSON.parse(fs.readFileSync('./test-data/jest-sample.json', 'utf-8'));
  const jestResult = parseJestJson(jestData);
  const report = generateMarkdownReport(jestResult, { projectName: 'test-project' });
  
  console.log(`✅ 报告生成成功 (${report.length} 字符)`);
  console.log('');
  console.log('--- 报告预览 ---');
  console.log(report.substring(0, 500));
  console.log('...');
  console.log('');
  
  // 保存报告
  const outputPath = './test-output/test-report.md';
  const outputDir = path.dirname(outputPath);
  if (!fs.existsSync(outputDir)) {
    fs.mkdirSync(outputDir, { recursive: true });
  }
  fs.writeFileSync(outputPath, report, 'utf-8');
  console.log(`✅ 报告已保存到: ${outputPath}\n`);
} catch (error) {
  console.log(`❌ 报告生成失败: ${error.message}\n`);
  process.exit(1);
}

console.log('✅ 所有验证通过！\n');