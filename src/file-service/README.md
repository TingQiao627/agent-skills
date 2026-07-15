# File Service - 文件上传下载服务

基于 Flask 的文件上传下载微服务，支持分片上传、断点续传和秒传功能。

## 功能特性

### 文件上传
- 基础上传（点击、拖拽、粘贴）
- 大文件分片上传（≥10MB 自动启用）
- 断点续传（7天有效期）
- 秒传（基于文件哈希）
- 文件类型和大小校验

### 文件下载
- 单文件下载
- 批量下载（打包为 ZIP）
- 下载进度显示
- 下载令牌验证

### 文件管理
- 文件列表（分页、搜索、排序）
- 文件删除（单个/批量）
- 文件预览（图片、PDF、文本）

## 快速开始

### 安装依赖
```bash
pip install flask werkzeug
```

### 启动服务
```bash
cd src/file-service
python routes/api.py
```

服务将在 `http://localhost:5000` 启动。

## API 端点

### 上传相关
- `POST /api/files/upload/init` - 初始化上传
- `POST /api/files/upload/chunk` - 上传分片
- `POST /api/files/upload/merge` - 合并分片
- `POST /api/files/upload/cancel` - 取消上传
- `POST /api/files/upload/check` - 检查文件是否存在（秒传）

### 下载相关
- `GET /api/files/download/<file_id>` - 下载文件
- `POST /api/files/download/batch` - 批量下载

### 管理相关
- `GET /api/files/list` - 文件列表
- `POST /api/files/delete` - 删除文件
- `GET /api/files/preview/<file_id>` - 文件预览

## 配置

通过环境变量配置：

| 变量 | 说明 | 默认值 |
|------|------|--------|
| UPLOAD_DIR | 文件存储目录 | ./uploads |
| MAX_FILE_SIZE | 最大文件大小 | 2GB |
| CHUNK_SIZE | 分片大小 | 5MB |
| RESUMABLE_UPLOAD_EXPIRE_DAYS | 断点续传有效期 | 7天 |

## 示例

### 初始化上传
```bash
curl -X POST http://localhost:5000/api/files/upload/init \
  -H "Content-Type: application/json" \
  -d '{
    "file_name": "example.pdf",
    "file_size": 5242880,
    "file_type": "application/pdf",
    "file_hash": "abc123...",
    "uploader": "user1"
  }'
```

### 上传分片
```bash
curl -X POST http://localhost:5000/api/files/upload/chunk \
  -F "upload_id=<upload_id>" \
  -F "chunk_index=0" \
  -F "chunk=@chunk_0.dat"
```

### 获取文件列表
```bash
curl "http://localhost:5000/api/files/list?page=1&page_size=20"
```

## 许可证

MIT