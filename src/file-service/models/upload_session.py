"""上传会话数据模型"""
from dataclasses import dataclass
from datetime import datetime
from typing import List, Optional


@dataclass
class UploadSession:
    """上传会话模型（用于分片上传和断点续传）"""
    upload_id: str
    file_hash: str
    file_name: str
    file_size: int
    file_type: str
    chunk_size: int
    total_chunks: int
    uploaded_chunks: List[int]  # 已上传的分片索引列表
    uploader: str
    created_at: datetime
    updated_at: datetime
    expires_at: datetime
    
    def to_dict(self) -> dict:
        """转换为字典"""
        return {
            "upload_id": self.upload_id,
            "file_hash": self.file_hash,
            "file_name": self.file_name,
            "file_size": self.file_size,
            "file_type": self.file_type,
            "chunk_size": self.chunk_size,
            "total_chunks": self.total_chunks,
            "uploaded_chunks": self.uploaded_chunks,
            "uploader": self.uploader,
            "created_at": self.created_at.isoformat(),
            "updated_at": self.updated_at.isoformat(),
            "expires_at": self.expires_at.isoformat()
        }
    
    @classmethod
    def from_dict(cls, data: dict) -> 'UploadSession':
        """从字典创建"""
        return cls(
            upload_id=data["upload_id"],
            file_hash=data["file_hash"],
            file_name=data["file_name"],
            file_size=data["file_size"],
            file_type=data["file_type"],
            chunk_size=data["chunk_size"],
            total_chunks=data["total_chunks"],
            uploaded_chunks=data["uploaded_chunks"],
            uploader=data["uploader"],
            created_at=datetime.fromisoformat(data["created_at"]),
            updated_at=datetime.fromisoformat(data["updated_at"]),
            expires_at=datetime.fromisoformat(data["expires_at"])
        )