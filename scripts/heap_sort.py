#!/usr/bin/env python3
"""
Heap Sort Algorithm Implementation

This module provides an implementation of the heap sort algorithm with O(n log n)
time complexity and O(1) space complexity for in-place sorting.

Example:
    >>> heap_sort([3, 1, 4, 1, 5, 9, 2, 6])
    [1, 1, 2, 3, 4, 5, 6, 9]
"""

from typing import List, Optional, Callable, Any


def heapify(arr: List[Any], n: int, i: int, 
            compare: Optional[Callable[[Any, Any], bool]] = None) -> None:
    """
    Maintain heap property for subtree rooted at index i.
    
    Args:
        arr: List to heapify
        n: Size of heap
        i: Root index of subtree
        compare: Optional comparison function. Returns True if first arg should be
                 prioritized over second. Default is max-heap behavior (a > b).
    
    Time Complexity: O(log n)
    Space Complexity: O(1)
    
    Example:
        >>> arr = [4, 10, 3, 5, 1]
        >>> heapify(arr, 5, 0)
        >>> arr[0]  # Max element at root
        10
    """
    if compare is None:
        compare = lambda a, b: a > b
    
    extreme = i
    left = 2 * i + 1
    right = 2 * i + 2
    
    # Check if left child is more extreme than root
    if left < n and compare(arr[left], arr[extreme]):
        extreme = left
    
    # Check if right child is more extreme than current extreme
    if right < n and compare(arr[right], arr[extreme]):
        extreme = right
    
    # Swap if root is not the extreme and recursively heapify affected subtree
    if extreme != i:
        arr[i], arr[extreme] = arr[extreme], arr[i]
        heapify(arr, n, extreme, compare)


def build_heap(arr: List[Any], 
               compare: Optional[Callable[[Any, Any], bool]] = None) -> None:
    """
    Build a heap from an unsorted array in-place.
    
    Args:
        arr: List to convert to heap
        compare: Optional comparison function (see heapify)
    
    Time Complexity: O(n)
    Space Complexity: O(1)
    
    Example:
        >>> arr = [4, 10, 3, 5, 1]
        >>> build_heap(arr)
        >>> arr[0]  # Max element at root
        10
    """
    n = len(arr)
    # Start from last non-leaf node and heapify each node in reverse order
    for i in range(n // 2 - 1, -1, -1):
        heapify(arr, n, i, compare)


def heap_sort(arr: List[Any], 
              compare: Optional[Callable[[Any, Any], bool]] = None,
              in_place: bool = False) -> List[Any]:
    """
    Sort an array using the heap sort algorithm.
    
    Args:
        arr: List to sort
        compare: Optional comparison function (see heapify). For ascending order,
                 use lambda a, b: a < b. Default is ascending order.
        in_place: If True, sorts the original array. If False, works on a copy.
    
    Returns:
        Sorted list (ascending by default)
    
    Time Complexity: O(n log n)
    Space Complexity: O(1) if in_place=True, O(n) otherwise
    
    Example:
        >>> heap_sort([3, 1, 4, 1, 5, 9, 2, 6])
        [1, 1, 2, 3, 4, 5, 6, 9]
        
        >>> heap_sort_descending([3, 1, 4])  # Descending order
        [4, 3, 1]
    """
    if not arr:
        return arr if in_place else []
    
    # Work on copy unless in_place is True
    result = arr if in_place else arr.copy()
    n = len(result)
    
    # For ascending sort, we use max-heap and extract max to end
    # So compare should be a > b for ascending (default max-heap behavior)
    if compare is None:
        # Default: ascending order using max-heap
        heap_compare = lambda a, b: a > b
    else:
        heap_compare = compare
    
    # Build heap
    build_heap(result, heap_compare)
    
    # Extract elements from heap one by one
    for i in range(n - 1, 0, -1):
        # Move current root (extreme) to end
        result[0], result[i] = result[i], result[0]
        # Heapify reduced heap
        heapify(result, i, 0, heap_compare)
    
    return result


def heap_sort_descending(arr: List[Any], in_place: bool = False) -> List[Any]:
    """
    Sort an array in descending order using heap sort.
    
    Args:
        arr: List to sort
        in_place: If True, sorts the original array
    
    Returns:
        Sorted list in descending order
    
    Time Complexity: O(n log n)
    Space Complexity: O(1) if in_place=True, O(n) otherwise
    
    Example:
        >>> heap_sort_descending([3, 1, 4, 1, 5])
        [5, 4, 3, 1, 1]
    """
    return heap_sort(arr, compare=lambda a, b: a < b, in_place=in_place)


if __name__ == "__main__":
    # Example usage
    import doctest
    doctest.testmod()
    
    # Demonstration
    print("Heap Sort Demonstration")
    print("-" * 40)
    
    # Ascending sort
    arr1 = [64, 34, 25, 12, 22, 11, 90]
    print(f"Original array: {arr1}")
    print(f"Sorted (ascending): {heap_sort(arr1)}")
    
    # Descending sort
    print(f"Sorted (descending): {heap_sort_descending(arr1)}")
    
    # In-place sort
    arr2 = [5, 2, 9, 1, 7]
    print(f"\nBefore in-place sort: {arr2}")
    heap_sort(arr2, in_place=True)
    print(f"After in-place sort: {arr2}")
    
    # Custom comparator (sort by absolute value)
    arr3 = [-5, 2, -9, 1, -7]
    print(f"\nOriginal: {arr3}")
    print(f"Sorted by absolute value: {heap_sort(arr3, compare=lambda a, b: abs(a) < abs(b))}")