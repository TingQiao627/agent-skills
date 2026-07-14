/**
 * 哈希函数算法实现
 * 提供多种哈希算法的示例实现
 */

/**
 * 简单的字符串哈希函数 (DJB2 算法)
 * @param {string} str - 要哈希的字符串
 * @returns {number} 哈希值
 */
function djb2Hash(str) {
  let hash = 5381;
  for (let i = 0; i < str.length; i++) {
    hash = ((hash << 5) + hash) + str.charCodeAt(i); // hash * 33 + c
  }
  return hash >>> 0; // 转换为无符号整数
}

/**
 * 简单的哈希函数 (采用取模法)
 * @param {string} str - 要哈希的字符串
 * @param {number} tableSize - 哈希表大小
 * @returns {number} 哈希值
 */
function simpleHash(str, tableSize = 1000) {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = (hash + str.charCodeAt(i)) % tableSize;
  }
  return hash;
}

/**
 * SHA-256 简化版实现演示
 * 注意：这不是真正的 SHA-256，仅用于教学目的
 * 生产环境请使用 crypto 模块
 */
function simpleSHA256(data) {
  // 简化的哈希逻辑
  let hash = 0;
  for (let i = 0; i < data.length; i++) {
    const char = data.charCodeAt(i);
    hash = ((hash << 5) - hash) + char;
    hash = hash & hash; // 转换为32位整数
  }
  // 转换为16进制字符串，模拟SHA-256的64字符输出
  const hex = Math.abs(hash).toString(16);
  return hex.padStart(64, '0').substring(0, 64);
}

/**
 * 模块导出
 */
module.exports = {
  djb2Hash,
  simpleHash,
  simpleSHA256
};

// 如果直接运行此文件，执行测试
if (require.main === module) {
  console.log('=== 哈希函数测试 ===\n');
  
  const testStrings = ['hello', 'world', 'test', '哈希函数'];
  
  testStrings.forEach(str => {
    console.log(`字符串: "${str}"`);
    console.log(`  DJB2 哈希: ${djb2Hash(str)}`);
    console.log(`  简单哈希: ${simpleHash(str)}`);
    console.log(`  SHA-256 (简化): ${simpleSHA256(str)}`);
    console.log();
  });
}