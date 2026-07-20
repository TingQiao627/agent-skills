# QuickSort 规格文档

## 1. Objective (目标)
实现一个通用的原地快速排序算法，支持任意可比较类型的列表排序。

## 2. Assumptions (假设)
- 编程语言：Python 3.10+
- 使用 Lomuto 分区方案实现原地排序
- 递归实现，基准元素选择最后一个元素（简单直观）
- 支持升序排序
- 包含单元测试（pytest）

## 3. Commands (接口)
```python
def quicksort(arr: list, low: int = 0, high: int | None = None) -> None:
    """原地快速排序，升序排列 arr[low:high+1]"""
    ...

def partition(arr: list, low: int, high: int) -> int:
    """Lomuto 分区：选择 arr[high] 为 pivot，返回 pivot 最终位置"""
    ...
```

## 4. Project Structure (项目结构)
```
src/
  quicksort.py      # 快速排序实现
tests/
  test_quicksort.py # 单元测试
specs/
  quicksort-spec.md # 本规格文档
```

## 5. Constraints (约束)
- 时间复杂度：平均 O(n log n)，最坏 O(n²)
- 空间复杂度：O(log n)（递归栈）
- 必须支持原地排序
- 输入列表元素必须支持 `<` 比较运算符

## 6. Acceptance Criteria (验收标准)
- [ ] 空列表排序结果为空
- [ ] 单元素列表排序后不变
- [ ] 已排序列表排序后不变
- [ ] 逆序列表正确排序
- [ ] 含重复元素列表正确排序
- [ ] 随机列表正确排序
- [ ] 大列表（1000+ 元素）排序正确