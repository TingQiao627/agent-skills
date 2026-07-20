"""快速排序 - 原地 Lomuto 分区方案实现。

提供 quicksort() 函数对任意可比较类型的列表进行原地升序排序。
"""

from typing import TypeVar

T = TypeVar("T")


def partition(arr: list[T], low: int, high: int) -> int:
    """Lomuto 分区：选择 arr[high] 为 pivot。

    将数组分为两部分：左侧 <= pivot，右侧 > pivot。
    返回 pivot 最终索引位置。

    Args:
        arr: 待分区列表
        low: 分区起始索引
        high: 分区结束索引（pivot 位置）

    Returns:
        pivot 的最终索引
    """
    pivot = arr[high]
    i = low - 1  # 较小元素的边界

    for j in range(low, high):
        if arr[j] <= pivot:
            i += 1
            arr[i], arr[j] = arr[j], arr[i]

    # 将 pivot 放到正确位置
    arr[i + 1], arr[high] = arr[high], arr[i + 1]
    return i + 1


def quicksort(arr: list[T], low: int = 0, high: int | None = None) -> None:
    """原地快速排序，升序排列 arr[low:high+1]。

    Args:
        arr: 待排序列表（原地修改）
        low: 排序起始索引，默认 0
        high: 排序结束索引，默认 len(arr) - 1
    """
    if high is None:
        high = len(arr) - 1

    if low < high:
        pivot_idx = partition(arr, low, high)
        quicksort(arr, low, pivot_idx - 1)
        quicksort(arr, pivot_idx + 1, high)