"""
T3 账号登录系统配置
"""
from pydantic_settings import BaseSettings
from typing import List


class Settings(BaseSettings):
    """应用配置"""
    # 应用配置
    APP_NAME: str = "T3 Account Login System"
    APP_VERSION: str = "1.0.0"
    DEBUG: bool = True
    
    # JWT 配置
    JWT_SECRET_KEY: str = "your-secret-key-change-in-production"
    JWT_ALGORITHM: str = "RS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 15
    REFRESH_TOKEN_EXPIRE_DAYS: int = 7
    
    # 密码配置
    BCRYPT_COST: int = 12
    
    # 登录安全配置
    MAX_LOGIN_ATTEMPTS: int = 5
    LOCK_TIME_MINUTES: int = 30
    MAX_LOCK_ATTEMPTS_24H: int = 10
    LOCK_TIME_24H_HOURS: int = 24
    
    # 限流配置
    RATE_LIMIT_IP_PER_MINUTE: int = 100
    RATE_LIMIT_ACCOUNT_PER_MINUTE: int = 10
    
    # 并发会话配置
    MAX_CONCURRENT_SESSIONS: int = 5
    
    # Redis 配置
    REDIS_URL: str = "redis://localhost:6379/0"
    
    # 白名单 IP（企业内网段）
    WHITELIST_IPS: List[str] = ["10.0.0.0/8", "172.16.0.0/12", "192.168.0.0/16"]
    
    class Config:
        env_file = ".env"


settings = Settings()