"""配置文件"""
import os

class Config:
    """应用配置"""
    # 文件存储配置
    UPLOAD_DIR = os.getenv("UPLOAD_DIR", "./uploads")
    CHUNK_DIR = os.getenv("CHUNK_DIR", "./chunks")
    
    # 上传限制
    MAX_FILE_SIZE = int(os.getenv("MAX_FILE_SIZE", 2 * 1024 * 1024 * 1024))  # 2GB
    CHUNK_SIZE = int(os.getenv("CHUNK_SIZE", 5 * 1024 * 1024))  # 5MB
    LARGE_FILE_THRESHOLD = int(os.getenv("LARGE_FILE_THRESHOLD", 10 * 1024 * 1024))  # 10MB
    
    # 分片上传配置
    MAX_CONCURRENT_UPLOADS = int(os.getenv("MAX_CONCURRENT_UPLOADS", 3))
    
    # 断点续传配置
    RESUMABLE_UPLOAD_EXPIRE_DAYS = int(os.getenv("RESUMABLE_UPLOAD_EXPIRE_DAYS", 7))
    
    # 批量下载限制
    MAX_BATCH_DOWNLOAD_COUNT = int(os.getenv("MAX_BATCH_DOWNLOAD_COUNT", 50))
    MAX_BATCH_DOWNLOAD_SIZE = int(os.getenv("MAX_BATCH_DOWNLOAD_SIZE", 500 * 1024 * 1024))  # 500MB
    
    # 下载链接配置
    DOWNLOAD_LINK_EXPIRE_SECONDS = int(os.getenv("DOWNLOAD_LINK_EXPIRE_SECONDS", 3600))  # 1小时
    
    # 允许的文件类型（白名单）
    ALLOWED_EXTENSIONS = {
        # 图片
        'jpg', 'jpeg', 'png', 'gif', 'webp', 'svg',
        # 文档
        'pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx',
        # 文本
        'txt', 'md', 'json', 'xml', 'csv',
        # 音视频
        'mp3', 'mp4', 'avi', 'mov',
        # 压缩包
        'zip', 'rar', '7z'
    }