"""
用户仓储服务（模拟数据库）
"""
from typing import Optional, Dict, List
from datetime import datetime
import sys
sys.path.insert(0, '..')
from models import User, UserCreate
from services import password_service


class UserRepository:
    """用户仓储（模拟实现）"""
    
    def __init__(self):
        """初始化用户存储"""
        self._users: Dict[int, User] = {}
        self._user_by_username: Dict[str, int] = {}
        self._user_by_email: Dict[str, int] = {}
        self._user_by_phone: Dict[str, int] = {}
        self._next_id = 1
        
        # 密码历史记录（用于重置密码时检查）
        self._password_history: Dict[int, List[str]] = {}
        
        # 活跃会话数
        self._active_sessions: Dict[int, int] = {}
    
    async def create_user(self, user_create: UserCreate) -> User:
        """
        创建用户
        
        Args:
            user_create: 用户创建请求
            
        Returns:
            创建的用户
        """
        # 加密密码
        password_hash = password_service.hash_password(user_create.password)
        
        user = User(
            id=self._next_id,
            username=user_create.username,
            email=user_create.email,
            phone=user_create.phone,
            password_hash=password_hash,
            created_at=datetime.now(),
            updated_at=datetime.now()
        )
        
        # 存储用户
        self._users[user.id] = user
        self._user_by_username[user.username] = user.id
        
        if user.email:
            self._user_by_email[user.email] = user.id
        
        if user.phone:
            self._user_by_phone[user.phone] = user.id
        
        # 初始化密码历史
        self._password_history[user.id] = [password_hash]
        
        # 初始化会话计数
        self._active_sessions[user.id] = 0
        
        self._next_id += 1
        
        return user
    
    async def find_by_username(self, username: str) -> Optional[User]:
        """根据用户名查找用户"""
        user_id = self._user_by_username.get(username)
        return self._users.get(user_id) if user_id else None
    
    async def find_by_email(self, email: str) -> Optional[User]:
        """根据邮箱查找用户"""
        user_id = self._user_by_email.get(email)
        return self._users.get(user_id) if user_id else None
    
    async def find_by_phone(self, phone: str) -> Optional[User]:
        """根据手机号查找用户"""
        user_id = self._user_by_phone.get(phone)
        return self._users.get(user_id) if user_id else None
    
    async def find_by_login_field(self, login_field: str) -> Optional[User]:
        """
        根据登录字段查找用户（支持用户名/邮箱/手机号）
        
        Args:
            login_field: 登录字段
            
        Returns:
            用户
        """
        # 尝试用户名
        user = await self.find_by_username(login_field)
        if user:
            return user
        
        # 尝试邮箱
        user = await self.find_by_email(login_field)
        if user:
            return user
        
        # 尝试手机号
        user = await self.find_by_phone(login_field)
        return user
    
    async def verify_password(self, user: User, password: str) -> bool:
        """
        验证用户密码
        
        Args:
            user: 用户
            password: 明文密码
            
        Returns:
            是否匹配
        """
        return password_service.verify_password(password, user.password_hash)
    
    async def increment_session(self, user_id: int) -> bool:
        """
        增加会话计数
        
        Args:
            user_id: 用户ID
            
        Returns:
            是否成功（未超过并发限制）
        """
        current = self._active_sessions.get(user_id, 0)
        if current >= 5:  # MAX_CONCURRENT_SESSIONS
            return False
        
        self._active_sessions[user_id] = current + 1
        return True
    
    async def decrement_session(self, user_id: int):
        """减少会话计数"""
        current = self._active_sessions.get(user_id, 0)
        if current > 0:
            self._active_sessions[user_id] = current - 1
    
    async def reset_sessions(self, user_id: int):
        """重置会话计数（全局登出）"""
        self._active_sessions[user_id] = 0
    
    async def update_password(self, user_id: int, new_password_hash: str):
        """更新密码"""
        if user_id in self._users:
            user = self._users[user_id]
            user.password_hash = new_password_hash
            user.updated_at = datetime.now()
            
            # 更新密码历史
            if user_id not in self._password_history:
                self._password_history[user_id] = []
            
            self._password_history[user_id].append(new_password_hash)


# 全局用户仓储实例
user_repository = UserRepository()