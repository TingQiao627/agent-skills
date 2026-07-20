import unittest
from quicksort import quicksort


class TestQuicksort(unittest.TestCase):
    """快速排序测试用例，覆盖 spec 中全部场景。"""

    def test_normal_array(self):
        """正常数组"""
        self.assertEqual(
            quicksort([3, 1, 4, 1, 5, 9, 2, 6]),
            [1, 1, 2, 3, 4, 5, 6, 9],
        )

    def test_empty_array(self):
        """空数组"""
        self.assertEqual(quicksort([]), [])

    def test_single_element(self):
        """单元素"""
        self.assertEqual(quicksort([42]), [42])

    def test_already_sorted(self):
        """已排序"""
        self.assertEqual(quicksort([1, 2, 3, 4, 5]), [1, 2, 3, 4, 5])

    def test_reverse_order(self):
        """逆序"""
        self.assertEqual(quicksort([5, 4, 3, 2, 1]), [1, 2, 3, 4, 5])

    def test_duplicates(self):
        """含重复元素"""
        self.assertEqual(
            quicksort([2, 3, 2, 1, 3]),
            [1, 2, 2, 3, 3],
        )

    def test_negative_numbers(self):
        """负数"""
        self.assertEqual(
            quicksort([-3, 0, 5, -1, 2]),
            [-3, -1, 0, 2, 5],
        )

    def test_inplace_modification(self):
        """验证原地排序 — 原数组引用被修改而非返回新数组"""
        arr = [3, 1, 2]
        result = quicksort(arr)
        self.assertIs(arr, result)
        self.assertEqual(arr, [1, 2, 3])


if __name__ == "__main__":
    unittest.main()