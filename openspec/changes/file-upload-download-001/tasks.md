# 任务清单

## Phase 1: 基础设施（估计：3天）

### 1.1 数据库设计
- [ ] 创建 `files` 表
  ```sql
  CREATE TABLE files (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    size BIGINT NOT NULL,
    type VARCHAR(100),
    hash VARCHAR(64),
    storage_key VARCHAR(500) NOT NULL,
    status ENUM('UPLOADING', 'COMPLETED', 'FAILED'),
    uploader VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
  );
  ```

- [ ] 创建 `upload_sessions` 表
  ```sql
  CREATE TABLE upload_sessions (
    id VARCHAR(36) PRIMARY KEY,
    file_hash VARCHAR(64) NOT NULL,
    file_name VARCHAR(255),
    total_size BIGINT NOT NULL,
    chunk_size INT NOT NULL,
    total_chunks INT NOT NULL,
    uploaded_chunks JSON,
    status ENUM('UPLOADING', 'COMPLETED', 'EXPIRED'),
    uploader VARCHAR(36),
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
  );
  ```

### 1.2 对象存储配置
- [ ] 配置 OSS/S3/MinIO 连接
- [ ] 创建存储桶（bucket）
- [ ] 配置 CORS 规则
- [ ] 配置生命周期策略（临时分片7天后清理）

### 1.3 API 框架搭建
- [ ] 创建文件服务模块结构
- [ ] 配置路由
- [ ] 实现基础中间件（认证、日志）

---

## Phase 2: 上传功能（估计：5天）

### 2.1 基础上传（2天）
- [ ] 实现 `/api/files/upload/init` 接口
- [ ] 实现 `/api/files/upload/chunk` 接口
- [ ] 实现 `/api/files/upload/merge` 接口
- [ ] 实现 `/api/files/upload/cancel` 接口
- [ ] 编写单元测试

### 2.2 大文件分片上传（1天）
- [ ] 实现分片逻辑
- [ ] 实现并发上传控制
- [ ] 实现分片合并
- [ ] 编写集成测试

### 2.3 断点续传（1天）
- [ ] 实现上传会话持久化
- [ ] 实现已上传分片查询
- [ ] 实现断点续传逻辑
- [ ] 编写测试

### 2.4 秒传（1天）
- [ ] 实现文件哈希查询
- [ ] 实现秒传逻辑
- [ ] 编写测试

---

## Phase 3: 下载功能（估计：2天）

### 3.1 基础下载（1天）
- [ ] 实现 `/api/files/download/{fileId}` 接口
- [ ] 实现下载签名验证
- [ ] 实现下载链接有效期控制
- [ ] 编写测试

### 3.2 批量下载（1天）
- [ ] 实现 `/api/files/download/batch` 接口
- [ ] 实现 ZIP 打包
- [ ] 实现批量下载限制
- [ ] 编写测试

---

## Phase 4: 预览与管理（估计：3天）

### 4.1 文件预览（2天）
- [ ] 实现 `/api/files/preview/{fileId}` 接口
- [ ] 集成图片预览
- [ ] 集成 PDF 预览（PDF.js）
- [ ] 集成 Office 文档预览
- [ ] 集成文本文件预览
- [ ] 集成音视频播放
- [ ] 编写测试

### 4.2 文件管理（1天）
- [ ] 实现 `/api/files/list` 接口
- [ ] 实现 `/api/files/delete` 接口
- [ ] 实现 `/api/files/rename` 接口
- [ ] 实现审计日志
- [ ] 编写测试

---

## Phase 5: 前端集成（估计：4天）

### 5.1 上传组件（2天）
- [ ] 实现文件选择（点击、拖拽、粘贴）
- [ ] 实现分片上传逻辑
- [ ] 实现进度显示
- [ ] 实现断点续传
- [ ] 实现秒传
- [ ] 编写组件测试

### 5.2 文件列表组件（1天）
- [ ] 实现文件列表展示
- [ ] 实现搜索、排序
- [ ] 实现批量操作
- [ ] 编写组件测试

### 5.3 预览组件（1天）
- [ ] 实现图片预览器
- [ ] 实现 PDF 预览器
- [ ] 实现 Office 文档预览器
- [ ] 实现文本预览器
- [ ] 实现音视频播放器
- [ ] 编写组件测试

---

## 测试验收清单

### 上传功能
- [ ] 点击上传按钮可以选择本地文件
- [ ] 拖拽文件到上传区域可以触发上传
- [ ] 粘贴图片可以触发上传
- [ ] 可以同时选择多个文件上传
- [ ] 上传过程显示实时进度条和速度
- [ ] 上传完成后显示成功状态
- [ ] 超过限制大小的文件被拒绝并提示
- [ ] 不允许的文件类型被拒绝并提示
- [ ] 大文件（>10MB）自动分片上传
- [ ] 上传中断后重新上传同一文件从断点继续
- [ ] 秒传：上传已存在的文件直接完成

### 下载功能
- [ ] 点击下载按钮触发下载
- [ ] 下载过程显示进度
- [ ] 可以取消下载
- [ ] 下载失败可以重试
- [ ] 批量下载生成 ZIP 包
- [ ] 下载链接过期后无法访问

### 预览功能
- [ ] 图片可以预览，支持缩放
- [ ] PDF 可以在线预览
- [ ] Office 文档可以预览
- [ ] 文本文件可以预览

### 管理功能
- [ ] 文件列表可以展示、搜索、排序
- [ ] 可以删除单个或多个文件
- [ ] 删除文件需要二次确认
- [ ] 可以重命名文件

---

## 风险与缓解

| 风险项 | 缓解措施 | 负责人 | 状态 |
|--------|----------|--------|------|
| 大文件上传带宽占用 | 分片并发控制，带宽限制配置 | - | 待处理 |
| 存储成本增长 | 设置文件生命周期策略 | - | 待处理 |
| 文件安全风险 | 服务端二次校验，权限控制 | - | 待处理 |

---

## 时间估算

| 阶段 | 预计时间 | 实际时间 | 偏差 |
|------|----------|----------|------|
| Phase 1: 基础设施 | 3天 | - | - |
| Phase 2: 上传功能 | 5天 | - | - |
| Phase 3: 下载功能 | 2天 | - | - |
| Phase 4: 预览与管理 | 3天 | - | - |
| Phase 5: 前端集成 | 4天 | - | - |
| **总计** | **17天** | - | - |