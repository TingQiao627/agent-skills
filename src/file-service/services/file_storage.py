"""文件存储服务"""
import os
import uuid
from datetime import datetime, timedelta
from typing import Optional, List, Dict, BinaryIO
from .config import Config
from .models.file import File, FileStatus
from .models.upload_session import UploadSession
from .utils import ensure_dir, get_file_extension


class FileStorage:
    """文件存储服务"""
    
    def __init__(self, config: Config = None):
        self.config = config or Config()
        self._files: Dict[str, File] = {}  # 内存存储
        self._sessions: Dict[str, UploadSession] = {}  # 上传会话存储
        
        # 确保存储目录存在
        ensure_dir(self.config.UPLOAD_DIR)
        ensure_dir(self.config.CHUNK_DIR)
    
    # ========== 上传相关 ==========
    
    def init_upload(self, file_name: str, file_size: int, file_type: str, 
                    file_hash: str, uploader: str) -> dict:
        """初始化上传会话
        
        Args:
            file_name: 文件名
            file_size: 文件大小
            file_type: 文件类型
            file_hash: 文件哈希值
            uploader: 上传者
        
        Returns:
            包含 upload_id 和上传信息的字典
        """
        # 检查是否可秒传
        existing = self.find_by_hash(file_hash)
        if existing:
            return {
                "instant_upload": True,
                "file_id": existing.id,
                "message": "秒传成功，文件已存在"
            }
        
        # 创建上传会话
        upload_id = str(uuid.uuid4())
        chunk_size = self.config.CHUNK_SIZE
        total_chunks = (file_size + chunk_size - 1) // chunk_size
        
        now = datetime.now()
        expires_at = now + timedelta(days=self.config.RESUMABLE_UPLOAD_EXPIRE_DAYS)
        
        session = UploadSession(
            upload_id=upload_id,
            file_hash=file_hash,
            file_name=file_name,
            file_size=file_size,
            file_type=file_type,
            chunk_size=chunk_size,
            total_chunks=total_chunks,
            uploaded_chunks=[],
            uploader=uploader,
            created_at=now,
            updated_at=now,
            expires_at=expires_at
        )
        
        self._sessions[upload_id] = session
        
        return {
            "instant_upload": False,
            "upload_id": upload_id,
            "chunk_size": chunk_size,
            "total_chunks": total_chunks,
            "expires_at": expires_at.isoformat()
        }
    
    def upload_chunk(self, upload_id: str, chunk_index: int, 
                     chunk_data: bytes, chunk_hash: str) -> dict:
        """上传分片
        
        Args:
            upload_id: 上传ID
            chunk_index: 分片索引
            chunk_data: 分片数据
            chunk_hash: 分片哈希
        
        Returns:
            上传结果
        """
        session = self._sessions.get(upload_id)
        if not session:
            return {"success": False, "error": "上传会话不存在"}
        
        if session.expires_at < datetime.now():
            del self._sessions[upload_id]
            return {"success": False, "error": "上传会话已过期"}
        
        # 保存分片
        chunk_dir = os.path.join(self.config.CHUNK_DIR, upload_id)
        ensure_dir(chunk_dir)
        chunk_path = os.path.join(chunk_dir, f"chunk_{chunk_index}")
        
        with open(chunk_path, 'wb') as f:
            f.write(chunk_data)
        
        # 更新会话
        if chunk_index not in session.uploaded_chunks:
            session.uploaded_chunks.append(chunk_index)
            session.updated_at = datetime.now()
        
        return {
            "success": True,
            "chunk_index": chunk_index,
            "uploaded_count": len(session.uploaded_chunks),
            "total_chunks": session.total_chunks
        }
    
    def merge_chunks(self, upload_id: str) -> dict:
        """合并分片
        
        Args:
            upload_id: 上传ID
        
        Returns:
            合并结果
        """
        session = self._sessions.get(upload_id)
        if not session:
            return {"success": False, "error": "上传会话不存在"}
        
        if len(session.uploaded_chunks) != session.total_chunks:
            return {
                "success": False, 
                "error": f"分片不完整，缺少分片: {set(range(session.total_chunks)) - set(session.uploaded_chunks)}"
            }
        
        # 创建文件记录
        file_id = str(uuid.uuid4())
        storage_key = f"{file_id}/{session.file_name}"
        file_path = os.path.join(self.config.UPLOAD_DIR, storage_key)
        ensure_dir(os.path.dirname(file_path))
        
        # 合并分片
        with open(file_path, 'wb') as outfile:
            for i in range(session.total_chunks):
                chunk_path = os.path.join(self.config.CHUNK_DIR, upload_id, f"chunk_{i}")
                with open(chunk_path, 'rb') as infile:
                    outfile.write(infile.read())
        
        # 创建文件记录
        now = datetime.now()
        file = File(
            id=file_id,
            name=session.file_name,
            size=session.file_size,
            type=session.file_type,
            hash=session.file_hash,
            storage_key=storage_key,
            status=FileStatus.COMPLETED,
            uploader=session.uploader,
            created_at=now,
            updated_at=now
        )
        
        self._files[file_id] = file
        
        # 清理临时分片
        chunk_dir = os.path.join(self.config.CHUNK_DIR, upload_id)
        for i in range(session.total_chunks):
            chunk_path = os.path.join(chunk_dir, f"chunk_{i}")
            if os.path.exists(chunk_path):
                os.remove(chunk_path)
        if os.path.exists(chunk_dir):
            os.rmdir(chunk_dir)
        
        # 删除上传会话
        del self._sessions[upload_id]
        
        return {
            "success": True,
            "file_id": file_id,
            "message": "文件上传完成"
        }
    
    def cancel_upload(self, upload_id: str) -> dict:
        """取消上传
        
        Args:
            upload_id: 上传ID
        
        Returns:
            取消结果
        """
        session = self._sessions.get(upload_id)
        if not session:
            return {"success": False, "error": "上传会话不存在"}
        
        # 清理临时分片
        chunk_dir = os.path.join(self.config.CHUNK_DIR, upload_id)
        if os.path.exists(chunk_dir):
            import shutil
            shutil.rmtree(chunk_dir)
        
        del self._sessions[upload_id]
        return {"success": True, "message": "上传已取消"}
    
    def check_file_exists(self, file_hash: str) -> dict:
        """检查文件是否存在（秒传检查）
        
        Args:
            file_hash: 文件哈希值
        
        Returns:
            检查结果
        """
        existing = self.find_by_hash(file_hash)
        if existing:
            return {
                "exists": True,
                "file_id": existing.id,
                "name": existing.name,
                "size": existing.size
            }
        return {"exists": False}
    
    # ========== 下载相关 ==========
    
    def get_download_info(self, file_id: str) -> Optional[dict]:
        """获取下载信息
        
        Args:
            file_id: 文件ID
        
        Returns:
            下载信息
        """
        file = self._files.get(file_id)
        if not file:
            return None
        
        return {
            "file_id": file.id,
            "name": file.name,
            "size": file.size,
            "type": file.type,
            "storage_key": file.storage_key,
            "path": os.path.join(self.config.UPLOAD_DIR, file.storage_key)
        }
    
    def create_download_token(self, file_id: str, expire_seconds: int = None) -> str:
        """创建下载令牌
        
        Args:
            file_id: 文件ID
            expire_seconds: 过期时间（秒）
        
        Returns:
            下载令牌
        """
        import time
        import hmac
        import hashlib
        
        expire_seconds = expire_seconds or self.config.DOWNLOAD_LINK_EXPIRE_SECONDS
        expires_at = int(time.time()) + expire_seconds
        
        # 简单令牌生成（生产环境应使用更安全的方案）
        token_data = f"{file_id}:{expires_at}"
        secret = "file-download-secret-key"  # 生产环境应从配置读取
        signature = hmac.new(secret.encode(), token_data.encode(), hashlib.sha256).hexdigest()[:16]
        
        return f"{file_id}.{expires_at}.{signature}"
    
    # ========== 文件管理 ==========
    
    def list_files(self, page: int = 1, page_size: int = 20, 
                   search: str = None, sort_by: str = "created_at",
                   sort_order: str = "desc") -> dict:
        """获取文件列表
        
        Args:
            page: 页码
            page_size: 每页数量
            search: 搜索关键词
            sort_by: 排序字段
            sort_order: 排序顺序
        
        Returns:
            文件列表信息
        """
        files = list(self._files.values())
        
        # 搜索过滤
        if search:
            files = [f for f in files if search.lower() in f.name.lower()]
        
        # 排序
        reverse = sort_order == "desc"
        if sort_by == "name":
            files.sort(key=lambda f: f.name, reverse=reverse)
        elif sort_by == "size":
            files.sort(key=lambda f: f.size, reverse=reverse)
        else:  # created_at
            files.sort(key=lambda f: f.created_at, reverse=reverse)
        
        # 分页
        total = len(files)
        start = (page - 1) * page_size
        end = start + page_size
        page_files = files[start:end]
        
        return {
            "files": [f.to_dict() for f in page_files],
            "total": total,
            "page": page,
            "page_size": page_size,
            "total_pages": (total + page_size - 1) // page_size
        }
    
    def delete_files(self, file_ids: List[str]) -> dict:
        """删除文件
        
        Args:
            file_ids: 文件ID列表
        
        Returns:
            删除结果
        """
        deleted = []
        not_found = []
        
        for file_id in file_ids:
            file = self._files.get(file_id)
            if file:
                # 删除物理文件
                file_path = os.path.join(self.config.UPLOAD_DIR, file.storage_key)
                if os.path.exists(file_path):
                    os.remove(file_path)
                
                del self._files[file_id]
                deleted.append(file_id)
            else:
                not_found.append(file_id)
        
        return {
            "deleted": deleted,
            "not_found": not_found,
            "message": f"成功删除 {len(deleted)} 个文件"
        }
    
    def find_by_hash(self, file_hash: str) -> Optional[File]:
        """根据哈希值查找文件
        
        Args:
            file_hash: 文件哈希值
        
        Returns:
            文件对象
        """
        for file in self._files.values():
            if file.hash == file_hash:
                return file
        return None
    
    def get_file(self, file_id: str) -> Optional[File]:
        """获取文件信息
        
        Args:
            file_id: 文件ID
        
        Returns:
            文件对象
        """
        return self._files.get(file_id)
    
    # ========== 会话恢复 ==========
    
    def get_upload_session(self, upload_id: str) -> Optional[UploadSession]:
        """获取上传会话
        
        Args:
            upload_id: 上传ID
        
        Returns:
            上传会话
        """
        session = self._sessions.get(upload_id)
        if session and session.expires_at >= datetime.now():
            return session
        return None