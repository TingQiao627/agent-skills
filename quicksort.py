from typing import List


def quicksort(arr: List[int]) -> List[int]:
    """对整数数组进行原地快速排序，返回排序后的数组。

    采用经典快速排序分治策略：
    1. 选取数组最后一个元素作为基准（pivot）
    2. 分区：将小于基准的元素移到左侧，大于基准的移到右侧
    3. 递归对左右子数组排序
    """
    _quicksort_helper(arr, 0, len(arr) - 1)
    return arr


def _quicksort_helper(arr: List[int], low: int, high: int) -> None:
    """递归快速排序辅助函数。"""
    if low < high:
        pivot_idx = _partition(arr, low, high)
        _quicksort_helper(arr, low, pivot_idx - 1)
        _quicksort_helper(arr, pivot_idx + 1, high)


def _partition(arr: List[int], low: int, high: int) -> int:
    """分区操作：以 arr[high] 为基准，返回基准最终位置。"""
    pivot = arr[high]
    i = low - 1  # 小于基准区域的右边界

    for j in range(low, high):
        if arr[j] <= pivot:
            i += 1
            arr[i], arr[j] = arr[j], arr[i]

    # 将基准放到正确位置
    arr[i + 1], arr[high] = arr[high], arr[i + 1]
    return i + 1