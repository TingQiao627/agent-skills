"""快速排序 - 纯 Python 断言测试"""

import sys
import os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), "..", "src"))

from quicksort import quicksort


def test_empty_list():
    arr = []
    quicksort(arr)
    assert arr == [], f"空列表测试失败: {arr}"

def test_single_element():
    arr = [42]
    quicksort(arr)
    assert arr == [42], f"单元素测试失败: {arr}"

def test_already_sorted():
    arr = [1, 2, 3, 4, 5]
    quicksort(arr)
    assert arr == [1, 2, 3, 4, 5], f"已排序测试失败: {arr}"

def test_reverse_sorted():
    arr = [5, 4, 3, 2, 1]
    quicksort(arr)
    assert arr == [1, 2, 3, 4, 5], f"逆序测试失败: {arr}"

def test_duplicates():
    arr = [3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5]
    quicksort(arr)
    assert arr == [1, 1, 2, 3, 3, 4, 5, 5, 5, 6, 9], f"重复元素测试失败: {arr}"

def test_random_small():
    arr = [9, 7, 5, 3, 1, 8, 6, 4, 2, 0]
    quicksort(arr)
    assert arr == [0, 1, 2, 3, 4, 5, 6, 7, 8, 9], f"随机小列表测试失败: {arr}"

def test_large_list():
    import random
    arr = list(range(1000))
    random.shuffle(arr)
    expected = sorted(arr)
    quicksort(arr)
    assert arr == expected, f"大列表测试失败: 长度 {len(arr)}"

def test_negative_numbers():
    arr = [-3, 5, -1, 0, 2, -8, 7]
    quicksort(arr)
    assert arr == [-8, -3, -1, 0, 2, 5, 7], f"负数测试失败: {arr}"

def test_floats():
    arr = [3.14, 1.41, 2.72, 0.0, -1.5]
    quicksort(arr)
    assert arr == [-1.5, 0.0, 1.41, 2.72, 3.14], f"浮点数测试失败: {arr}"

def test_strings():
    arr = ["banana", "apple", "cherry", "date"]
    quicksort(arr)
    assert arr == ["apple", "banana", "cherry", "date"], f"字符串测试失败: {arr}"

def test_original_list_modified():
    arr = [3, 1, 2]
    result = arr
    quicksort(arr)
    assert arr is result, "原地排序失败: 对象引用变化"
    assert arr == [1, 2, 3], f"原地排序失败: {arr}"


if __name__ == "__main__":
    tests = [
        test_empty_list, test_single_element, test_already_sorted,
        test_reverse_sorted, test_duplicates, test_random_small,
        test_large_list, test_negative_numbers, test_floats,
        test_strings, test_original_list_modified
    ]
    passed = 0
    for t in tests:
        try:
            t()
            passed += 1
            print(f"  ✅ {t.__name__}")
        except AssertionError as e:
            print(f"  ❌ {t.__name__}: {e}")
    print(f"\n{passed}/{len(tests)} 测试通过")