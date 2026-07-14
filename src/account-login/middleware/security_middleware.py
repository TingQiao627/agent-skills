"""
安全防护中间件
实现登录失败锁定、IP 限流、账号限流
"""
from fastapi import Request, HTTPException
from datetime import datetime, timedelta
from typing import Dict, Optional
import asyncio
from config import settings


class SecurityMiddleware:
    """安全防护中间件"""
    
    def __init__(self):
        """初始化"""
        # 登录失败记录 {account: {"count": int, "first_failure": datetime, "locked_until": datetime}}
        self._login_failures: Dict[str, dict] = {}
        
        # IP 限流记录 {ip: {"count": int, "reset_time": datetime}}
        self._ip_rate_limit: Dict[str, dict] = {}
        
        # 账号限流记录 {account: {"count": int, "reset_time": datetime}}
        self._account_rate_limit: Dict[str, dict] = {}
        
        # 锁
        self._lock = asyncio.Lock()
    
    async def check_ip_rate_limit(self, client_ip: str) -> bool:
        """
        检查 IP 限流
        
        Args:
            client_ip: 客户端 IP
            
        Returns:
            是否允许访问（True=允许，False=限流）
        """
        async with self._lock:
            now = datetime.now()
            
            # 检查是否存在记录
            if client_ip not in self._ip_rate_limit:
                self._ip_rate_limit[client_ip] = {"count": 1, "reset_time": now + timedelta(minutes=1)}
                return True
            
            record = self._ip_rate_limit[client_ip]
            
            # 检查是否超过重置时间
            if now > record["reset_time"]:
                record["count"] = 1
                record["reset_time"] = now + timedelta(minutes=1)
                return True
            
            # 检查是否超过限制
            if record["count"] >= settings.RATE_LIMIT_IP_PER_MINUTE:
                return False
            
            # 增加计数
            record["count"] += 1
            return True
    
    async def check_account_rate_limit(self, account: str) -> bool:
        """
        检查账号限流
        
        Args:
            account: 账号
            
        Returns:
            是否允许访问
        """
        async with self._lock:
            now = datetime.now()
            
            if account not in self._account_rate_limit:
                self._account_rate_limit[account] = {"count": 1, "reset_time": now + timedelta(minutes=1)}
                return True
            
            record = self._account_rate_limit[account]
            
            if now > record["reset_time"]:
                record["count"] = 1
                record["reset_time"] = now + timedelta(minutes=1)
                return True
            
            if record["count"] >= settings.RATE_LIMIT_ACCOUNT_PER_MINUTE:
                return False
            
            record["count"] += 1
            return True
    
    async def check_account_locked(self, account: str) -> bool:
        """
        检查账号是否被锁定
        
        Args:
            account: 账号
            
        Returns:
            是否被锁定（True=锁定，False=未锁定）
        """
        async with self._lock:
            if account not in self._login_failures:
                return False
            
            record = self._login_failures[account]
            
            # 检查锁定时间
            if "locked_until" in record:
                if datetime.now() < record["locked_until"]:
                    return True
                else:
                    # 锁定已过期，清除记录
                    del self._login_failures[account]
                    return False
            
            return False
    
    async def record_login_failure(self, account: str) -> tuple[bool, int]:
        """
        记录登录失败
        
        Args:
            account: 账号
            
        Returns:
            (是否触发锁定, 锁定时长分钟数)
        """
        async with self._lock:
            now = datetime.now()
            
            # 初始化记录
            if account not in self._login_failures:
                self._login_failures[account] = {"count": 0, "first_failure": now}
            
            record = self._login_failures[account]
            
            # 检查是否超过30分钟（重置计数）
            if now - record["first_failure"] > timedelta(minutes=30):
                record["count"] = 0
                record["first_failure"] = now
            
            # 增加失败次数
            record["count"] += 1
            
            # 检查是否触发锁定
            if record["count"] >= settings.MAX_LOCK_ATTEMPTS_24H:
                # 锁定24小时
                record["locked_until"] = now + timedelta(hours=settings.LOCK_TIME_24H_HOURS)
                return True, settings.LOCK_TIME_24H_HOURS * 60
            
            elif record["count"] >= settings.MAX_LOGIN_ATTEMPTS:
                # 锁定30分钟
                record["locked_until"] = now + timedelta(minutes=settings.LOCK_TIME_MINUTES)
                return True, settings.LOCK_TIME_MINUTES
            
            return False, 0
    
    async def clear_login_failures(self, account: str):
        """清除登录失败记录（登录成功后调用）"""
        async with self._lock:
            if account in self._login_failures:
                del self._login_failures[account]
    
    async def get_remaining_attempts(self, account: str) -> int:
        """获取剩余尝试次数"""
        async with self._lock:
            if account not in self._login_failures:
                return settings.MAX_LOGIN_ATTEMPTS
            
            record = self._login_failures[account]
            remaining = settings.MAX_LOGIN_ATTEMPTS - record.get("count", 0)
            return max(0, remaining)


# 全局安全中间件实例
security_middleware = SecurityMiddleware()


async def check_security(request: Request, account: str):
    """
    安全检查装饰器
    
    Args:
        request: 请求对象
        account: 账号
        
    Raises:
        HTTPException: 安全检查失败
    """
    client_ip = request.client.host if request.client else "unknown"
    
    # 检查 IP 限流
    if not await security_middleware.check_ip_rate_limit(client_ip):
        raise HTTPException(status_code=429, detail="请求过于频繁，请稍后再试")
    
    # 检查账号限流
    if not await security_middleware.check_account_rate_limit(account):
        raise HTTPException(status_code=429, detail="登录尝试过于频繁")
    
    # 检查账号锁定
    if await security_middleware.check_account_locked(account):
        raise HTTPException(status_code=403, detail="账号已被锁定，请稍后再试")