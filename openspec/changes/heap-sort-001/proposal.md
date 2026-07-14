# OPSX: Heap Sort Algorithm Implementation

## Metadata
- **ID**: heap-sort-001
- **Status**: proposed
- **Created**: 2026-07-14
- **Priority**: medium
- **Complexity**: low

## Summary
实现堆排序算法，提供高效的原地排序功能。

## Context
### Background
当前 scripts/ 目录包含验证和评估脚本，但缺少算法实现示例。堆排序是经典的比较排序算法，时间复杂度 O(n log n)，适合作为算法示例添加到仓库。

### Motivation
- 提供可复用的排序算法实现
- 作为技能仓库中的算法示例
- 展示最佳实践代码结构

## Scope
### In Scope
- 堆排序核心实现（heapify, heap sort）
- 单元测试覆盖边界条件
- 使用文档和示例

### Out of Scope
- 其他排序算法实现
- 性能基准测试
- GUI/CLI 接口

## Technical Design
### Approach
使用 Python 实现，采用标准的原地堆排序算法：

1. **build_max_heap**: 将数组转换为最大堆
2. **heapify**: 维护堆性质
3. **heap_sort**: 主排序函数

### Data Structures
- 输入：可迭代序列（转换为列表）
- 输出：排序后的列表（原地排序）

### Algorithm
```
heap_sort(arr):
    n = len(arr)
    # Build max heap
    for i in range(n//2-1, -1, -1):
        heapify(arr, n, i)
    # Extract elements from heap
    for i in range(n-1, 0, -1):
        arr[0], arr[i] = arr[i], arr[0]
        heapify(arr, i, 0)

heapify(arr, n, i):
    largest = i
    left = 2*i + 1
    right = 2*i + 2
    if left < n and arr[left] > arr[largest]:
        largest = left
    if right < n and arr[right] > arr[largest]:
        largest = right
    if largest != i:
        arr[i], arr[largest] = arr[largest], arr[i]
        heapify(arr, n, largest)
```

### Files to Create
- `scripts/heap_sort.py` - 核心实现
- `tests/test_heap_sort.py` - 单元测试

## Acceptance Criteria
- [ ] 实现 heapify 函数
- [ ] 实现 heap_sort 函数
- [ ] 处理空数组和单元素数组
- [ ] 处理已排序和逆序数组
- [ ] 单元测试覆盖率 ≥ 90%
- [ ] 代码符合 PEP 8 规范

## Risks & Mitigations
| Risk | Impact | Mitigation |
|------|--------|------------|
| 递归深度过大 | 低 | Python 默认递归限制足够处理大规模数据 |
| 类型不兼容 | 低 | 添加类型检查和文档说明 |

## Implementation Checklist
- [ ] 创建 `scripts/heap_sort.py`
- [ ] 实现 heapify 函数
- [ ] 实现 heap_sort 函数
- [ ] 创建 `tests/test_heap_sort.py`
- [ ] 编写单元测试（正常、边界、异常场景）
- [ ] 添加使用文档和示例

## References
- CLRS Introduction to Algorithms, Chapter 6
- Python Sorting HOW TO: https://docs.python.org/3/howto/sorting.html