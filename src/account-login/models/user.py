"""
用户实体模型
"""
from datetime import datetime
from typing import Optional
from pydantic import BaseModel, Field, validator
import re


class User(BaseModel):
    """用户实体"""
    id: Optional[int] = None
    username: str = Field(..., min_length=4, max_length=32, description="用户名")
    email: Optional[str] = Field(None, description="邮箱")
    phone: Optional[str] = Field(None, description="手机号")
    password_hash: str = Field(..., description="密码哈希")
    is_active: bool = Field(default=True, description="是否激活")
    created_at: datetime = Field(default_factory=datetime.now, description="创建时间")
    updated_at: datetime = Field(default_factory=datetime.now, description="更新时间")
    
    @validator('username')
    def validate_username(cls, v):
        """验证用户名：4-32字符，字母数字下划线"""
        if not re.match(r'^[a-zA-Z0-9_]{4,32}$', v):
            raise ValueError('用户名必须是4-32位字母、数字或下划线')
        return v
    
    @validator('email')
    def validate_email(cls, v):
        """验证邮箱格式"""
        if v and not re.match(r'^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$', v):
            raise ValueError('邮箱格式不正确')
        return v
    
    @validator('phone')
    def validate_phone(cls, v):
        """验证手机号：中国大陆11位"""
        if v and not re.match(r'^1[3-9]\d{9}$', v):
            raise ValueError('手机号格式不正确')
        return v


class UserCreate(BaseModel):
    """用户创建请求"""
    username: str = Field(..., min_length=4, max_length=32)
    password: str = Field(..., min_length=8)
    email: Optional[str] = None
    phone: Optional[str] = None
    
    @validator('password')
    def validate_password(cls, v):
        """密码强度验证：最少8位，含大小写字母+数字"""
        if len(v) < 8:
            raise ValueError('密码至少8位')
        if not re.search(r'[a-z]', v):
            raise ValueError('密码必须包含小写字母')
        if not re.search(r'[A-Z]', v):
            raise ValueError('密码必须包含大写字母')
        if not re.search(r'\d', v):
            raise ValueError('密码必须包含数字')
        return v


class LoginRequest(BaseModel):
    """登录请求"""
    login_field: str = Field(..., description="登录字段（用户名/邮箱/手机号）")
    password: str = Field(..., description="密码")
    remember_me: bool = Field(default=False, description="记住密码")
    captcha: Optional[str] = Field(None, description="图形验证码")


class LoginResponse(BaseModel):
    """登录响应"""
    access_token: str
    refresh_token: str
    token_type: str = "Bearer"
    expires_in: int
    user: dict


class TokenPayload(BaseModel):
    """Token 载荷"""
    user_id: int
    username: str
    exp: datetime
    type: str  # access 或 refresh