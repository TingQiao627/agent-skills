"""文件服务入口"""
from .routes.api import app
from .config import Config
from .services.file_storage import FileStorage

__version__ = '1.0.0'

__all__ = ['app', 'Config', 'FileStorage']