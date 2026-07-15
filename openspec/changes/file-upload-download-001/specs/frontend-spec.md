# 前端技术规格

## 上传组件

### 文件选择方式

1. **点击上传**：input[type=file]
2. **拖拽上传**：drag & drop API
3. **粘贴上传**：clipboard API

### 分片上传实现

```javascript
class ChunkUploader {
  constructor(file, options) {
    this.file = file;
    this.chunkSize = options.chunkSize || 5 * 1024 * 1024; // 5MB
    this.concurrency = options.concurrency || 3;
    this.chunks = [];
    this.uploadedChunks = [];
  }

  // 计算文件哈希
  async calculateHash() {
    return new SparkMD5.ArrayBuffer().append(this.file).end();
  }

  // 创建分片
  createChunks() {
    const chunks = [];
    let offset = 0;
    while (offset < this.file.size) {
      chunks.push(this.file.slice(offset, offset + this.chunkSize));
      offset += this.chunkSize;
    }
    this.chunks = chunks;
  }

  // 上传单个分片
  async uploadChunk(chunkIndex) {
    const chunk = this.chunks[chunkIndex];
    const formData = new FormData();
    formData.append('uploadId', this.uploadId);
    formData.append('chunkIndex', chunkIndex);
    formData.append('chunkData', chunk);
    
    return fetch('/api/files/upload/chunk', {
      method: 'POST',
      body: formData
    });
  }

  // 并发上传
  async uploadWithConcurrency() {
    const queue = [...Array(this.chunks.length).keys()];
    const active = new Set();
    
    while (queue.length > 0 || active.size > 0) {
      while (active.size < this.concurrency && queue.length > 0) {
        const chunkIndex = queue.shift();
        active.add(chunkIndex);
        this.uploadChunk(chunkIndex).then(() => {
          active.delete(chunkIndex);
          this.uploadedChunks.push(chunkIndex);
          this.updateProgress();
        });
      }
      await new Promise(resolve => setTimeout(resolve, 100));
    }
  }
}
```

### 进度显示

```javascript
class UploadProgress {
  constructor(uploader) {
    this.uploader = uploader;
  }

  updateProgress() {
    const uploaded = this.uploader.uploadedChunks.length;
    const total = this.uploader.chunks.length;
    const percent = (uploaded / total * 100).toFixed(2);
    
    console.log(`已上传: ${uploaded}/${total} 分片 (${percent}%)`);
  }

  formatSize(bytes) {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB';
    return (bytes / 1024 / 1024).toFixed(2) + ' MB';
  }
}
```

---

## 下载组件

### 进度监控

```javascript
class DownloadManager {
  async download(fileId, onProgress) {
    const response = await fetch(`/api/files/download/${fileId}`);
    const reader = response.body.getReader();
    const contentLength = response.headers.get('Content-Length');
    
    let receivedLength = 0;
    let chunks = [];
    
    while(true) {
      const {done, value} = await reader.read();
      if (done) break;
      
      chunks.push(value);
      receivedLength += value.length;
      onProgress(receivedLength, contentLength);
    }
    
    return new Blob(chunks);
  }
}
```

---

## 预览组件

### 文件类型识别

```javascript
const PREVIEW_TYPES = {
  image: ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'image/svg+xml'],
  pdf: ['application/pdf'],
  office: [
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'application/vnd.openxmlformats-officedocument.presentationml.presentation'
  ],
  text: ['text/plain', 'text/html', 'text/css', 'text/javascript'],
  media: ['video/mp4', 'audio/mpeg']
};

function getPreviewType(mimeType) {
  for (const [type, mimes] of Object.entries(PREVIEW_TYPES)) {
    if (mimes.includes(mimeType)) return type;
  }
  return 'unknown';
}
```

### 预览器实现

```javascript
class FilePreviewer {
  preview(file, container) {
    const type = getPreviewType(file.type);
    
    switch (type) {
      case 'image':
        return this.previewImage(file, container);
      case 'pdf':
        return this.previewPDF(file, container);
      case 'office':
        return this.previewOffice(file, container);
      case 'text':
        return this.previewText(file, container);
      case 'media':
        return this.previewMedia(file, container);
      default:
        return this.showFileInfo(file, container);
    }
  }

  previewImage(file, container) {
    const img = document.createElement('img');
    img.src = URL.createObjectURL(file);
    container.appendChild(img);
  }

  previewPDF(file, container) {
    // 使用 PDF.js
    pdfjsLib.getDocument(file.url).promise.then(pdf => {
      pdf.getPage(1).then(page => {
        const canvas = document.createElement('canvas');
        const context = canvas.getContext('2d');
        page.render({ canvasContext: context, viewport: page.getViewport(1) });
        container.appendChild(canvas);
      });
    });
  }
}
```

---

## 断点续传实现

### 本地状态存储

```javascript
class UploadStateStorage {
  save(uploadId, state) {
    localStorage.setItem(`upload_${uploadId}`, JSON.stringify({
      uploadId,
      fileName: state.fileName,
      fileHash: state.fileHash,
      uploadedChunks: state.uploadedChunks,
      timestamp: Date.now()
    }));
  }

  load(uploadId) {
    const data = localStorage.getItem(`upload_${uploadId}`);
    return data ? JSON.parse(data) : null;
  }

  clear(uploadId) {
    localStorage.removeItem(`upload_${uploadId}`);
  }

  // 清理过期状态（7天）
  cleanup() {
    const now = Date.now();
    const expireTime = 7 * 24 * 60 * 60 * 1000;
    
    Object.keys(localStorage)
      .filter(key => key.startsWith('upload_'))
      .forEach(key => {
        const data = JSON.parse(localStorage.getItem(key));
        if (now - data.timestamp > expireTime) {
          localStorage.removeItem(key);
        }
      });
  }
}
```