# Examples 目录

本目录包含技能仓库的示例实现。

## 文件说明

### helloworld.js
基础的 HelloWorld 程序，演示基本的输出功能。

**运行方式:**
```bash
node examples/helloworld.js
```

### hash-functions.js
哈希函数算法实现，包括：
- DJB2 哈希算法
- 简单取模哈希
- SHA-256 简化版演示

**运行方式:**
```bash
node examples/hash-functions.js
```

## 注意事项

- 这些是教学示例，不适合直接用于生产环境
- 生产环境的哈希功能请使用 Node.js 的 `crypto` 模块
- 安全敏感场景请使用经过验证的加密库