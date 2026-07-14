"""
登录控制器
"""
from fastapi import APIRouter, HTTPException, Request, Response
from typing import Optional
import sys
sys.path.insert(0, '..')
from models import LoginRequest, LoginResponse, User
from services import password_service, jwt_service
from services.user_repository import user_repository
from config import settings


router = APIRouter()


@router.post("/login", response_model=LoginResponse)
async def login(login_request: LoginRequest, request: Request):
    """
    账号密码登录
    
    支持用户名/邮箱/手机号登录
    """
    # 获取客户端信息
    client_ip = request.client.host if request.client else "unknown"
    user_agent = request.headers.get("user-agent", "")
    device_fingerprint = f"{user_agent}:{client_ip}"
    
    # 查找用户
    user = await user_repository.find_by_login_field(login_request.login_field)
    
    if not user:
        raise HTTPException(status_code=401, detail="用户名或密码错误")
    
    if not user.is_active:
        raise HTTPException(status_code=403, detail="账号已被禁用")
    
    # 验证密码
    is_valid = await user_repository.verify_password(user, login_request.password)
    
    if not is_valid:
        # TODO: 记录登录失败次数，触发锁定机制
        raise HTTPException(status_code=401, detail="用户名或密码错误")
    
    # 检查并发会话限制
    can_create_session = await user_repository.increment_session(user.id)
    if not can_create_session:
        raise HTTPException(status_code=403, detail="已达到最大并发会话数")
    
    # 生成 JWT Token
    access_token = jwt_service.create_access_token(user.id, user.username)
    refresh_token = jwt_service.create_refresh_token(user.id, user.username, device_fingerprint)
    
    # 如果选择记住密码，设置持久化 Cookie
    response_data = {
        "access_token": access_token,
        "refresh_token": refresh_token,
        "token_type": "Bearer",
        "expires_in": settings.ACCESS_TOKEN_EXPIRE_MINUTES * 60,
        "user": {
            "id": user.id,
            "username": user.username,
            "email": user.email,
            "phone": user.phone
        }
    }
    
    return LoginResponse(**response_data)


@router.post("/refresh")
async def refresh_token(request: Request):
    """
    刷新 Access Token
    """
    # 从 Cookie 获取 refresh_token
    # TODO: 实现 HttpOnly Cookie 方式
    
    # 从请求体获取
    body = await request.json()
    refresh_token = body.get("refresh_token")
    
    if not refresh_token:
        raise HTTPException(status_code=401, detail="缺少 refresh token")
    
    # 获取设备指纹
    client_ip = request.client.host if request.client else "unknown"
    user_agent = request.headers.get("user-agent", "")
    device_fingerprint = f"{user_agent}:{client_ip}"
    
    # 刷新 token
    new_access_token = jwt_service.refresh_access_token(refresh_token, device_fingerprint)
    
    if not new_access_token:
        raise HTTPException(status_code=401, detail="refresh token 无效或已过期")
    
    return {
        "access_token": new_access_token,
        "token_type": "Bearer",
        "expires_in": settings.ACCESS_TOKEN_EXPIRE_MINUTES * 60
    }


@router.post("/logout")
async def logout(request: Request):
    """
    登出当前会话
    """
    # TODO: 实现 token 黑名单机制
    # TODO: 减少会话计数
    
    return {"message": "登出成功"}


@router.post("/logout-all")
async def logout_all(request: Request):
    """
    全局登出（撤销所有 Refresh Token）
    """
    # TODO: 实现全局登出逻辑
    # 1. 撤销所有 refresh token
    # 2. 清除客户端 Cookie
    # 3. 记录登出日志
    
    return {"message": "已从所有设备登出"}