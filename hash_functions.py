#!/usr/bin/env python3
"""
哈希函数算法实现
包含多种经典哈希算法的实现
"""


def simple_hash(data: str, table_size: int = 256) -> int:
    """
    简单哈希函数 - 使用乘法哈希
    时间复杂度: O(n), n为字符串长度
    """
    hash_value = 0
    for char in data:
        hash_value = (hash_value * 31 + ord(char)) % table_size
    return hash_value


def djb2_hash(data: str) -> int:
    """
    DJB2 哈希算法 - 由 Daniel J. Bernstein 设计
    简单高效的字符串哈希函数
    时间复杂度: O(n)
    """
    hash_value = 5381
    for char in data:
        hash_value = ((hash_value << 5) + hash_value) + ord(char)  # hash * 33 + c
    return hash_value & 0xFFFFFFFF  # 保持在32位范围内


def sdbm_hash(data: str) -> int:
    """
    SDBM 哈希算法 - 用于数据库索引
    时间复杂度: O(n)
    """
    hash_value = 0
    for char in data:
        hash_value = ord(char) + (hash_value << 6) + (hash_value << 16) - hash_value
    return hash_value & 0xFFFFFFFF


def fnv1a_hash(data: str, prime: int = 16777619, offset: int = 2166136261) -> int:
    """
    FNV-1a 哈希算法 - Fowler-Noll-Vo
    适用于快速哈希计算
    时间复杂度: O(n)
    """
    hash_value = offset
    for char in data:
        hash_value ^= ord(char)
        hash_value = (hash_value * prime) & 0xFFFFFFFF
    return hash_value


def murmur_like_hash(data: str, seed: int = 0) -> int:
    """
    类 Murmur 哈希算法的简化实现
    具有良好的分布性
    时间复杂度: O(n)
    """
    hash_value = seed
    for char in data:
        hash_value ^= ord(char)
        hash_value = ((hash_value * 0x5bd1e995) & 0xFFFFFFFF)
        hash_value ^= hash_value >> 15
    hash_value ^= hash_value >> 13
    hash_value = ((hash_value * 0xc2b2ae35) & 0xFFFFFFFF)
    hash_value ^= hash_value >> 16
    return hash_value


def hash_with_collision_test(data: str, hash_func, iterations: int = 1000) -> dict:
    """
    测试哈希函数的碰撞率
    返回碰撞统计信息
    """
    hashes = set()
    collisions = 0
    
    for i in range(iterations):
        test_data = f"{data}_{i}"
        h = hash_func(test_data)
        if h in hashes:
            collisions += 1
        hashes.add(h)
    
    return {
        "total_tests": iterations,
        "unique_hashes": len(hashes),
        "collisions": collisions,
        "collision_rate": collisions / iterations if iterations > 0 else 0
    }


def main():
    """演示哈希函数的使用"""
    test_strings = ["hello", "world", "test", "hash", "algorithm"]
    
    print("=" * 60)
    print("哈希函数算法演示")
    print("=" * 60)
    
    for s in test_strings:
        print(f"\n字符串: '{s}'")
        print(f"  Simple Hash: {simple_hash(s)}")
        print(f"  DJB2 Hash:   {djb2_hash(s)}")
        print(f"  SDBM Hash:   {sdbm_hash(s)}")
        print(f"  FNV-1a Hash: {fnv1a_hash(s)}")
        print(f"  Murmur-like: {murmur_like_hash(s)}")
    
    print("\n" + "=" * 60)
    print("碰撞测试 (DJB2)")
    print("=" * 60)
    stats = hash_with_collision_test("test", djb2_hash, 1000)
    print(f"总测试数: {stats['total_tests']}")
    print(f"唯一哈希: {stats['unique_hashes']}")
    print(f"碰撞数:   {stats['collisions']}")
    print(f"碰撞率:   {stats['collision_rate']:.4f}")


if __name__ == "__main__":
    main()