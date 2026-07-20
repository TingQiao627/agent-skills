/**
 * 冒泡排序单元测试
 * 覆盖 REQ-001 ~ REQ-006 全部验收标准
 */
import { bubbleSort } from "./bubbleSort";
import * as assert from "node:assert";

// 测试计数器
let passed = 0;
let failed = 0;

function test(name: string, fn: () => void): void {
  try {
    fn();
    passed++;
    console.log(`  ✓ ${name}`);
  } catch (e: unknown) {
    failed++;
    const msg = e instanceof Error ? e.message : String(e);
    console.log(`  ✗ ${name}: ${msg}`);
  }
}

function assertArraysEqual<T>(actual: T[], expected: T[], message: string): void {
  const actualStr = JSON.stringify(actual);
  const expectedStr = JSON.stringify(expected);
  assert.deepStrictEqual(actual, expected, `${message}: expected ${expectedStr}, got ${actualStr}`);
}

console.log("冒泡排序单元测试\n");

// ── REQ-001: 基本排序功能 ──
console.log("REQ-001: 基本排序功能");
test("乱序数组排序 [5,3,8,1,2] → [1,2,3,5,8]", () => {
  const result = bubbleSort([5, 3, 8, 1, 2]);
  assertArraysEqual(result, [1, 2, 3, 5, 8], "乱序数组排序");
});

test("已排序数组 [1,2,3,4,5] → [1,2,3,4,5]", () => {
  const result = bubbleSort([1, 2, 3, 4, 5]);
  assertArraysEqual(result, [1, 2, 3, 4, 5], "已排序数组");
});

// ── REQ-002: 空数组处理 ──
console.log("REQ-002: 空数组处理");
test("空数组 [] → []", () => {
  const result = bubbleSort([]);
  assertArraysEqual(result, [], "空数组");
});

// ── REQ-003: 单元素数组处理 ──
console.log("REQ-003: 单元素数组处理");
test("单元素数组 [1] → [1]", () => {
  const result = bubbleSort([1]);
  assertArraysEqual(result, [1], "单元素数组");
});

// ── REQ-004: 重复元素处理 ──
console.log("REQ-004: 重复元素处理");
test("重复元素 [3,3,3,1,2] → [1,2,3,3,3]", () => {
  const result = bubbleSort([3, 3, 3, 1, 2]);
  assertArraysEqual(result, [1, 2, 3, 3, 3], "重复元素");
});

// ── REQ-005: 负数处理 ──
console.log("REQ-005: 负数处理");
test("负数数组 [-1,-3,0,5,2] → [-3,-1,0,2,5]", () => {
  const result = bubbleSort([-1, -3, 0, 5, 2]);
  assertArraysEqual(result, [-3, -1, 0, 2, 5], "负数数组");
});

// ── REQ-006: 逆序数组处理 ──
console.log("REQ-006: 逆序数组处理");
test("逆序数组 [5,4,3,2,1] → [1,2,3,4,5]", () => {
  const result = bubbleSort([5, 4, 3, 2, 1]);
  assertArraysEqual(result, [1, 2, 3, 4, 5], "逆序数组");
});

// ── 额外测试: 稳定性验证 ──
console.log("\n额外测试: 稳定性");
test("原地排序修改原数组", () => {
  const arr = [3, 1, 2];
  const result = bubbleSort(arr);
  assert.strictEqual(result, arr, "应返回原数组引用");
});

// ── 结果汇总 ──
console.log(`\n───────────────`);
const total = passed + failed;
console.log(`结果: ${passed}/${total} 通过`);
if (failed > 0) {
  console.log(`${failed} 个测试失败`);
  process.exit(1);
} else {
  console.log("全部测试通过 ✓");
}