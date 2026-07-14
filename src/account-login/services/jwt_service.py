"""
JWT 双 Token 服务
实现 Access Token 和 Refresh Token 机制
"""
import jwt
from datetime import datetime, timedelta
from typing import Optional, Dict, Any
import secrets
from config import settings


class JwtService:
    """JWT 服务"""
    
    def __init__(self):
        """初始化 JWT 服务"""
        self.secret_key = settings.JWT_SECRET_KEY
        self.algorithm = settings.JWT_ALGORITHM
        self.access_token_expire = settings.ACCESS_TOKEN_EXPIRE_MINUTES
        self.refresh_token_expire = settings.REFRESH_TOKEN_EXPIRE_DAYS
    
    def create_access_token(self, user_id: int, username: str, additional_claims: Optional[Dict[str, Any]] = None) -> str:
        """
        创建 Access Token
        
        Args:
            user_id: 用户ID
            username: 用户名
            additional_claims: 额外声明
            
        Returns:
            Access Token
        """
        expire = datetime.utcnow() + timedelta(minutes=self.access_token_expire)
        
        payload = {
            "user_id": user_id,
            "username": username,
            "type": "access",
            "exp": expire,
            "iat": datetime.utcnow(),
            "jti": secrets.token_hex(16)  # JWT ID，用于防重放
        }
        
        if additional_claims:
            payload.update(additional_claims)
        
        token = jwt.encode(payload, self.secret_key, algorithm=self.algorithm)
        return token
    
    def create_refresh_token(self, user_id: int, username: str, device_fingerprint: str) -> str:
        """
        创建 Refresh Token
        
        Args:
            user_id: 用户ID
            username: 用户名
            device_fingerprint: 设备指纹
            
        Returns:
            Refresh Token
        """
        expire = datetime.utcnow() + timedelta(days=self.refresh_token_expire)
        
        payload = {
            "user_id": user_id,
            "username": username,
            "type": "refresh",
            "device_fingerprint": device_fingerprint,
            "exp": expire,
            "iat": datetime.utcnow(),
            "jti": secrets.token_hex(16)
        }
        
        token = jwt.encode(payload, self.secret_key, algorithm=self.algorithm)
        return token
    
    def verify_token(self, token: str, token_type: str = "access") -> Optional[Dict[str, Any]]:
        """
        验证 Token
        
        Args:
            token: JWT Token
            token_type: Token 类型（access/refresh）
            
        Returns:
            解码后的 payload，验证失败返回 None
        """
        try:
            payload = jwt.decode(token, self.secret_key, algorithms=[self.algorithm])
            
            # 验证 token 类型
            if payload.get("type") != token_type:
                return None
            
            return payload
            
        except jwt.ExpiredSignatureError:
            return None
        except jwt.InvalidTokenError:
            return None
    
    def refresh_access_token(self, refresh_token: str, device_fingerprint: str) -> Optional[str]:
        """
        使用 Refresh Token 刷新 Access Token
        
        Args:
            refresh_token: Refresh Token
            device_fingerprint: 设备指纹
            
        Returns:
            新的 Access Token，失败返回 None
        """
        payload = self.verify_token(refresh_token, "refresh")
        
        if not payload:
            return None
        
        # 验证设备指纹（可选，用于安全性增强）
        if payload.get("device_fingerprint") != device_fingerprint:
            return None
        
        # 创建新的 Access Token
        new_access_token = self.create_access_token(
            user_id=payload["user_id"],
            username=payload["username"]
        )
        
        return new_access_token
    
    def decode_token_without_verification(self, token: str) -> Optional[Dict[str, Any]]:
        """
        解码 Token（不验证签名，仅用于获取过期 token 的信息）
        
        Args:
            token: JWT Token
            
        Returns:
            解码后的 payload
        """
        try:
            # 不验证签名和过期时间
            payload = jwt.decode(token, options={"verify_signature": False, "verify_exp": False})
            return payload
        except jwt.InvalidTokenError:
            return None
    
    def get_token_expire_time(self, token: str) -> Optional[datetime]:
        """
        获取 Token 过期时间
        
        Args:
            token: JWT Token
            
        Returns:
            过期时间
        """
        payload = self.decode_token_without_verification(token)
        if payload and "exp" in payload:
            return datetime.fromtimestamp(payload["exp"])
        return None
    
    def is_token_expiring_soon(self, token: str, minutes_before: int = 5) -> bool:
        """
        检查 Token 是否即将过期
        
        Args:
            token: JWT Token
            minutes_before: 提前多少分钟判定为即将过期
            
        Returns:
            是否即将过期
        """
        expire_time = self.get_token_expire_time(token)
        if not expire_time:
            return True
        
        return expire_time <= datetime.utcnow() + timedelta(minutes=minutes_before)


# 全局 JWT 服务实例
jwt_service = JwtService()