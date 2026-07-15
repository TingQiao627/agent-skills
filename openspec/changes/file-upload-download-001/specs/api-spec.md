# API 技术规格

## 文件上传接口

### 1. 初始化上传

**POST** `/api/files/upload/init`

请求：
```json
{
  "fileName": "report.pdf",
  "fileSize": 5242880,
  "fileHash": "sha256:abc123...",
  "contentType": "application/pdf"
}
```

响应：
```json
{
  "uploadId": "upload-uuid-123",
  "chunkSize": 5242880,
  "status": "UPLOADING",
  "uploadedChunks": [1, 2, 3]
}
```

### 2. 上传分片

**POST** `/api/files/upload/chunk`

请求：
```
Content-Type: multipart/form-data

uploadId: upload-uuid-123
chunkIndex: 4
chunkData: [binary data]
```

响应：
```json
{
  "chunkIndex": 4,
  "status": "SUCCESS"
}
```

### 3. 合并分片

**POST** `/api/files/upload/merge`

请求：
```json
{
  "uploadId": "upload-uuid-123",
  "fileName": "report.pdf",
  "fileHash": "sha256:abc123..."
}
```

响应：
```json
{
  "fileId": "file-uuid-456",
  "name": "report.pdf",
  "size": 5242880,
  "url": "/api/files/download/file-uuid-456",
  "status": "COMPLETED"
}
```

### 4. 检查文件（秒传）

**POST** `/api/files/upload/check`

请求：
```json
{
  "fileHash": "sha256:abc123..."
}
```

响应（文件存在）：
```json
{
  "exists": true,
  "fileId": "file-uuid-789",
  "url": "/api/files/download/file-uuid-789"
}
```

响应（文件不存在）：
```json
{
  "exists": false
}
```

---

## 文件下载接口

### 1. 单文件下载

**GET** `/api/files/download/{fileId}?token={token}`

响应：文件流

### 2. 批量下载

**POST** `/api/files/download/batch`

请求：
```json
{
  "fileIds": ["file-uuid-1", "file-uuid-2", "file-uuid-3"]
}
```

响应：ZIP 文件流

---

## 文件管理接口

### 1. 文件列表

**GET** `/api/files/list?page=1&size=20&sort=created_at&order=desc&search=report`

响应：
```json
{
  "items": [
    {
      "id": "file-uuid-1",
      "name": "report.pdf",
      "size": 5242880,
      "type": "application/pdf",
      "status": "COMPLETED",
      "uploader": "user-1",
      "created_at": "2026-07-15T10:00:00Z"
    }
  ],
  "total": 100,
  "page": 1,
  "size": 20
}
```

### 2. 删除文件

**POST** `/api/files/delete`

请求：
```json
{
  "fileIds": ["file-uuid-1", "file-uuid-2"]
}
```

响应：
```json
{
  "deleted": 2,
  "failed": 0
}
```

### 3. 重命名文件

**POST** `/api/files/rename`

请求：
```json
{
  "fileId": "file-uuid-1",
  "newName": "report-2026.pdf"
}
```

响应：
```json
{
  "id": "file-uuid-1",
  "name": "report-2026.pdf",
  "updated_at": "2026-07-15T11:00:00Z"
}
```

---

## 文件预览接口

**GET** `/api/files/preview/{fileId}`

响应：
- 图片：返回图片流
- PDF：返回 PDF 流
- Office：返回预览链接或转换为 PDF
- 文本：返回文本内容
- 音视频：返回流式内容