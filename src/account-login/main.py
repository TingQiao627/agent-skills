"""
T3 账号登录系统主应用
"""
from fastapi import FastAPI
from controllers import router as auth_router
from config import settings

# 创建 FastAPI 应用
app = FastAPI(
    title=settings.APP_NAME,
    version=settings.APP_VERSION,
    description="T3 级账号登录系统，支持账号密码、短信验证码、OAuth 第三方登录"
)

# 注册路由
app.include_router(auth_router, prefix="/api/auth", tags=["认证"])


@app.get("/")
async def root():
    """根路径"""
    return {
        "message": "T3 Account Login System",
        "version": settings.APP_VERSION,
        "docs": "/docs"
    }


@app.get("/health")
async def health_check():
    """健康检查"""
    return {"status": "healthy"}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)