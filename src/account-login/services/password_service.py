"""
密码加密服务
"""
from passlib.context import CryptContext
from typing import Tuple
import secrets
import hashlib
from config import settings


class PasswordService:
    """密码加密服务"""
    
    def __init__(self):
        """初始化密码上下文"""
        self.pwd_context = CryptContext(
            schemes=["bcrypt"],
            deprecated="auto",
            bcrypt__cost=settings.BCRYPT_COST
        )
    
    def hash_password(self, password: str) -> str:
        """
        加密密码
        
        Args:
            password: 明文密码
            
        Returns:
            加密后的密码哈希
        """
        return self.pwd_context.hash(password)
    
    def verify_password(self, plain_password: str, hashed_password: str) -> bool:
        """
        验证密码
        
        Args:
            plain_password: 明文密码
            hashed_password: 哈希密码
            
        Returns:
            是否匹配
        """
        return self.pwd_context.verify(plain_password, hashed_password)
    
    def needs_rehash(self, hashed_password: str) -> bool:
        """
        检查是否需要重新哈希（cost 不匹配时）
        
        Args:
            hashed_password: 哈希密码
            
        Returns:
            是否需要重新哈希
        """
        return self.pwd_context.needs_update(hashed_password)
    
    @staticmethod
    def generate_remember_token(user_id: int, device_fingerprint: str) -> str:
        """
        生成记住密码 token（AES-256 加密）
        
        Args:
            user_id: 用户ID
            device_fingerprint: 设备指纹（User-Agent + IP段）
            
        Returns:
            记住密码 token
        """
        # 生成随机 token
        random_token = secrets.token_hex(32)
        
        # 组合设备指纹和用户ID
        data = f"{user_id}:{device_fingerprint}:{random_token}"
        
        # 使用 SHA256 哈希作为 token
        token = hashlib.sha256(data.encode()).hexdigest()
        
        return token
    
    @staticmethod
    def validate_password_history(new_password: str, password_history: list, max_history: int = 3) -> bool:
        """
        验证新密码是否与最近几次密码相同
        
        Args:
            new_password: 新密码
            password_history: 密码历史列表
            max_history: 最大历史记录数
            
        Returns:
            是否通过验证（True=可用，False=与历史密码相同）
        """
        if not password_history:
            return True
            
        pwd_service = PasswordService()
        
        # 检查最近 max_history 个密码
        recent_passwords = password_history[-max_history:]
        
        for old_hash in recent_passwords:
            if pwd_service.verify_password(new_password, old_hash):
                return False
        
        return True


# 全局密码服务实例
password_service = PasswordService()