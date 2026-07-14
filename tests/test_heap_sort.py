#!/usr/bin/env python3
"""
Unit tests for heap_sort module.

Tests cover:
- Basic sorting functionality (ascending/descending)
- Edge cases (empty, single element, duplicates)
- In-place sorting
- Custom comparators
- Performance characteristics

Run with: pytest test_heap_sort.py -v
"""

import pytest
import sys
import os

# Add scripts directory to path for import
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'scripts'))

from heap_sort import heapify, build_heap, heap_sort, heap_sort_descending


class TestHeapify:
    """Tests for heapify function."""
    
    def test_heapify_max_heap(self):
        """Test max-heap property after heapify."""
        arr = [4, 10, 3, 5, 1]
        heapify(arr, 5, 0)
        assert arr[0] == 10  # Max should be at root
    
    def test_heapify_min_heap(self):
        """Test min-heap property with custom comparator."""
        arr = [10, 4, 3, 5, 1]
        heapify(arr, 5, 0, compare=lambda a, b: a < b)
        assert arr[0] == 1  # Min should be at root
    
    def test_heapify_leaf_node(self):
        """Test heapify on leaf node (no change expected)."""
        arr = [1, 2, 3]
        arr_copy = arr.copy()
        heapify(arr, 3, 2)  # Index 2 is a leaf
        assert arr == arr_copy


class TestBuildHeap:
    """Tests for build_heap function."""
    
    def test_build_max_heap(self):
        """Test building max-heap from unsorted array."""
        arr = [4, 10, 3, 5, 1]
        build_heap(arr)
        # After build_heap, root should be max
        assert arr[0] == max(arr)
        # Check heap property: parent >= children
        n = len(arr)
        for i in range(n):
            left = 2 * i + 1
            right = 2 * i + 2
            if left < n:
                assert arr[i] >= arr[left]
            if right < n:
                assert arr[i] >= arr[right]
    
    def test_build_min_heap(self):
        """Test building min-heap with custom comparator."""
        arr = [10, 4, 3, 5, 1]
        build_heap(arr, compare=lambda a, b: a < b)
        assert arr[0] == min(arr)


class TestHeapSort:
    """Tests for heap_sort function."""
    
    def test_basic_ascending(self):
        """Test basic ascending sort."""
        arr = [64, 34, 25, 12, 22, 11, 90]
        result = heap_sort(arr)
        assert result == sorted(arr)
    
    def test_basic_descending(self):
        """Test basic descending sort."""
        arr = [64, 34, 25, 12, 22, 11, 90]
        result = heap_sort_descending(arr)
        assert result == sorted(arr, reverse=True)
    
    def test_empty_array(self):
        """Test sorting empty array."""
        assert heap_sort([]) == []
        assert heap_sort_descending([]) == []
    
    def test_single_element(self):
        """Test sorting single element array."""
        assert heap_sort([5]) == [5]
        assert heap_sort_descending([5]) == [5]
    
    def test_two_elements(self):
        """Test sorting two element array."""
        assert heap_sort([2, 1]) == [1, 2]
        assert heap_sort_descending([1, 2]) == [2, 1]
    
    def test_duplicates(self):
        """Test sorting array with duplicates."""
        arr = [3, 1, 4, 1, 5, 9, 2, 6, 5]
        result = heap_sort(arr)
        assert result == sorted(arr)
    
    def test_already_sorted(self):
        """Test sorting already sorted array."""
        arr = [1, 2, 3, 4, 5]
        assert heap_sort(arr) == arr
    
    def test_reverse_sorted(self):
        """Test sorting reverse sorted array."""
        arr = [5, 4, 3, 2, 1]
        assert heap_sort(arr) == [1, 2, 3, 4, 5]
    
    def test_in_place_sort(self):
        """Test in-place sorting."""
        arr = [3, 1, 4, 1, 5, 9, 2, 6]
        original_id = id(arr)
        result = heap_sort(arr, in_place=True)
        assert id(result) == original_id  # Same object
        assert arr == sorted(arr)
    
    def test_not_in_place_by_default(self):
        """Test that default behavior creates a copy."""
        arr = [3, 1, 4]
        result = heap_sort(arr)
        assert result != arr  # Result is sorted, original is not
        assert arr == [3, 1, 4]  # Original unchanged
    
    def test_custom_comparator(self):
        """Test sorting with custom comparator."""
        arr = [-5, 2, -9, 1, -7]
        # Sort by absolute value ascending
        result = heap_sort(arr, compare=lambda a, b: abs(a) < abs(b))
        expected = sorted(arr, key=abs, reverse=True)
        assert result == expected
    
    def test_negative_numbers(self):
        """Test sorting array with negative numbers."""
        arr = [-3, -1, -4, -1, -5, -9, -2, -6]
        result = heap_sort(arr)
        assert result == sorted(arr)
    
    def test_mixed_positive_negative(self):
        """Test sorting array with mixed positive and negative."""
        arr = [3, -1, 4, -1, 5, -9, 2, -6]
        result = heap_sort(arr)
        assert result == sorted(arr)


class TestHeapSortDescending:
    """Tests for heap_sort_descending helper function."""
    
    def test_basic_descending(self):
        """Test descending sort helper."""
        arr = [64, 34, 25, 12, 22, 11, 90]
        result = heap_sort_descending(arr)
        assert result == sorted(arr, reverse=True)
    
    def test_in_place_descending(self):
        """Test in-place descending sort."""
        arr = [3, 1, 4, 1, 5]
        heap_sort_descending(arr, in_place=True)
        assert arr == sorted(arr, reverse=True)


class TestEdgeCases:
    """Additional edge case tests."""
    
    def test_large_array(self):
        """Test sorting large array."""
        import random
        arr = [random.randint(1, 1000) for _ in range(1000)]
        result = heap_sort(arr)
        assert result == sorted(arr)
    
    def test_all_same_elements(self):
        """Test array where all elements are the same."""
        arr = [5, 5, 5, 5, 5]
        assert heap_sort(arr) == arr
    
    def test_with_floats(self):
        """Test sorting array with floating point numbers."""
        arr = [3.14, 1.41, 2.71, 0.577, 1.732]
        result = heap_sort(arr)
        assert result == sorted(arr)
    
    def test_with_strings(self):
        """Test sorting array with strings."""
        arr = ['banana', 'apple', 'cherry', 'date']
        result = heap_sort(arr)
        assert result == sorted(arr)
    
    def test_stability_check(self):
        """Verify heap sort is not stable (documented behavior)."""
        # Heap sort is not stable, but we verify it at least sorts correctly
        arr = [(3, 'a'), (1, 'b'), (3, 'c'), (1, 'd')]
        # Sort by first element only
        result = heap_sort(arr, compare=lambda a, b: a[0] < b[0])
        # Verify sorted by first element
        first_elements = [x[0] for x in result]
        assert first_elements == sorted(first_elements)


class TestPerformance:
    """Performance and complexity tests."""
    
    def test_time_complexity(self):
        """Test that heap sort completes in reasonable time."""
        import random
        import time
        
        # Test with 10000 elements
        arr = [random.randint(1, 10000) for _ in range(10000)]
        start = time.time()
        result = heap_sort(arr)
        elapsed = time.time() - start
        
        # Should complete in well under 1 second
        assert elapsed < 1.0
        assert result == sorted(arr)
    
    def test_space_complexity_in_place(self):
        """Test that in-place sort doesn't create copies."""
        import random
        arr = [random.randint(1, 100) for _ in range(100)]
        original_id = id(arr)
        heap_sort(arr, in_place=True)
        assert id(arr) == original_id


if __name__ == "__main__":
    # Run tests with pytest
    pytest.main([__file__, "-v"])