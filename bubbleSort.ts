/**
 * 冒泡排序 - 标准冒泡排序算法
 * 时间复杂度 O(n²)，空间复杂度 O(1)（原地排序），稳定排序
 *
 * @param arr - 待排序的整数数组
 * @returns 升序排列后的数组引用（原地排序，修改原数组）
 */
export function bubbleSort(arr: number[]): number[] {
  const n = arr.length;

  // 外层循环：遍历 n-1 次
  for (let i = 0; i < n - 1; i++) {
    // 内层循环：比较相邻元素，范围逐轮缩小
    // 已排序的尾部不再参与比较
    for (let j = 0; j < n - 1 - i; j++) {
      // 若前者大于后者则交换（保证稳定性：相等时不交换）
      if (arr[j] > arr[j + 1]) {
        const temp = arr[j];
        arr[j] = arr[j + 1];
        arr[j + 1] = temp;
      }
    }
  }

  return arr;
}