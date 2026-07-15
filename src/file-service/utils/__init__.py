"""工具函数"""
import hashlib
import os
from typing import BinaryIO


def calculate_file_hash(file: BinaryIO, algorithm: str = "sha256") -> str:
    """计算文件哈希值
    
    Args:
        file: 文件对象
        algorithm: 哈希算法 (sha256, md5)
    
    Returns:
        哈希值字符串
    """
    hash_obj = hashlib.new(algorithm)
    file.seek(0)
    
    while chunk := file.read(8192):
        hash_obj.update(chunk)
    
    file.seek(0)
    return hash_obj.hexdigest()


def calculate_chunk_hash(chunk: bytes, algorithm: str = "sha256") -> str:
    """计算分片哈希值
    
    Args:
        chunk: 分片数据
        algorithm: 哈希算法
    
    Returns:
        哈希值字符串
    """
    hash_obj = hashlib.new(algorithm)
    hash_obj.update(chunk)
    return hash_obj.hexdigest()


def ensure_dir(path: str) -> None:
    """确保目录存在
    
    Args:
        path: 目录路径
    """
    os.makedirs(path, exist_ok=True)


def get_file_extension(filename: str) -> str:
    """获取文件扩展名
    
    Args:
        filename: 文件名
    
    Returns:
        扩展名（小写，不含点）
    """
    if '.' not in filename:
        return ''
    return filename.rsplit('.', 1)[1].lower()


def format_file_size(size_bytes: int) -> str:
    """格式化文件大小显示
    
    Args:
        size_bytes: 字节数
    
    Returns:
        格式化后的字符串
    """
    for unit in ['B', 'KB', 'MB', 'GB', 'TB']:
        if size_bytes < 1024.0:
            return f"{size_bytes:.1f}{unit}"
        size_bytes /= 1024.0
    return f"{size_bytes:.1f}PB"