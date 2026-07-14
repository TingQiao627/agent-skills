# 解析器接口规范

本文档定义测试框架解析器的统一接口规范。

## 接口定义

### parse(resultFile) → TestResult

**参数**:
- `resultFile` (string): 测试结果文件路径

**返回值**: `TestResult` 对象

**异常**:
- 文件读取失败
- JSON/XML 解析失败
- 数据格式不兼容

## TestResult 结构

```javascript
{
  // 必填字段
  framework: string,       // 'jest' | 'vitest' | 'pytest' | 'junit'
  version: string,         // 框架版本，无法获取时为 '未获取'
  total: number,           // 用例总数
  passed: number,          // 通过数
  failed: number,          // 失败数
  skipped: number,         // 跳过数
  duration: number,        // 总耗时（毫秒）
  passRate: number,        // 通过率（百分比，如 95.5）
  
  // 测试文件列表
  testFiles: [{
    path: string,          // 测试文件路径
    tests: [{
      name: string,        // 用例名
      status: 'passed' | 'failed' | 'skipped',
      duration: number,    // 耗时（毫秒）
      error?: {            // 失败时必填
        message: string,   // 错误信息
        stack: string[]    // 堆栈关键行（最多 5 行）
      }
    }]
  }],
  
  // 可选字段
  coverage?: {             // 覆盖率数据
    statements: number,    // 语句覆盖率（百分比）
    branches: number,      // 分支覆盖率
    functions: number,     // 函数覆盖率
    lines: number,         // 行覆盖率
    lowCoverageFiles: [{   // 低于阈值文件
      path: string,
      coverage: number
    }]
  }
}
```

## 解析器实现要求

### 1. 健壮性

- 字段缺失时使用默认值，不要抛出异常
- 数值字段缺失时使用 `0` 或 `'未获取'`
- 数组字段缺失时使用空数组 `[]`
- 捕获所有解析异常并抛出明确错误信息

### 2. 性能

- 解析 1000 用例的结果文件应在 1 秒内完成
- 避免多次读取文件
- 使用流式解析处理大文件（可选）

### 3. 安全

- 不修改原始结果文件
- 不执行外部命令
- 不依赖网络请求

### 4. 兼容性

- 支持框架的多个版本
- 处理不同输出格式的差异
- 提供降级策略

## 错误处理

### 必须抛出的错误

```javascript
// 文件不存在
throw new Error(`读取文件失败: ${error.message}\n文件: ${resultFile}`);

// JSON 解析失败
throw new Error(`解析 JSON 文件失败: ${error.message}\n文件: ${resultFile}`);

// XML 解析失败
throw new Error(`解析 XML 文件失败: ${error.message}\n文件: ${resultFile}`);
```

### 降级处理

```javascript
// 字段缺失时
const version = result.version || '未获取';
const duration = result.duration || 0;

// 数组缺失时
const testFiles = result.testResults || [];
```

## 示例实现

参见 `scripts/parsers/jest-vitest.js` 和 `scripts/parsers/junit-xml.js`

## 测试用例

每个解析器应通过以下测试：

1. **正常解析**: 解析有效的结果文件，返回正确的 TestResult
2. **字段缺失**: 解析部分字段缺失的文件，返回降级结果
3. **文件损坏**: 解析损坏的文件，抛出明确错误
4. **空文件**: 解析空文件，返回空 TestResult
5. **大文件**: 解析 1000+ 用例的大文件，性能符合要求