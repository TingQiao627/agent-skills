# Design: 快速排序算法

## Algorithm

采用经典快速排序（QuickSort）分治策略：

1. **选择基准（Pivot）**：选取数组最后一个元素作为基准
2. **分区（Partition）**：
   - 遍历数组，将小于基准的元素移到左侧，大于基准的元素移到右侧
   - 返回基准元素的最终位置
3. **递归**：对左右两个子数组递归执行上述步骤

## Complexity

| 情况 | 时间复杂度 | 空间复杂度 |
|------|-----------|-----------|
| 最优 | O(n log n) | O(log n) |
| 平均 | O(n log n) | O(log n) |
| 最差 | O(n²) | O(n) |

## Interface

```python
def quicksort(arr: List[int]) -> List[int]:
    """对整数数组进行快速排序，返回排序后的数组"""
```

## Language

Python 3 — 简洁清晰，适合算法演示。