"""文件数据模型"""
from dataclasses import dataclass
from datetime import datetime
from enum import Enum
from typing import Optional


class FileStatus(Enum):
    """文件状态枚举"""
    UPLOADING = "UPLOADING"
    COMPLETED = "COMPLETED"
    FAILED = "FAILED"


@dataclass
class File:
    """文件模型"""
    id: str
    name: str
    size: int
    type: str  # MIME type
    hash: str  # SHA256 hash
    storage_key: str  # Storage path/key
    status: FileStatus
    uploader: str
    created_at: datetime
    updated_at: datetime
    
    def to_dict(self) -> dict:
        """转换为字典"""
        return {
            "id": self.id,
            "name": self.name,
            "size": self.size,
            "type": self.type,
            "hash": self.hash,
            "storage_key": self.storage_key,
            "status": self.status.value,
            "uploader": self.uploader,
            "created_at": self.created_at.isoformat(),
            "updated_at": self.updated_at.isoformat()
        }
    
    @classmethod
    def from_dict(cls, data: dict) -> 'File':
        """从字典创建"""
        return cls(
            id=data["id"],
            name=data["name"],
            size=data["size"],
            type=data["type"],
            hash=data["hash"],
            storage_key=data["storage_key"],
            status=FileStatus(data["status"]),
            uploader=data["uploader"],
            created_at=datetime.fromisoformat(data["created_at"]),
            updated_at=datetime.fromisoformat(data["updated_at"])
        )