# 代码评审报告 - 文件上传下载功能

**评审日期**: 2026-07-15  
**评审范围**: FileController.java, File.java, FileRepository.java, FileService.java  
**对应需求**: 文件上传下载功能需求文档 v1.0

---

## 📊 总体评估

| 维度 | 评分 | 说明 |
|------|------|------|
| 功能完整性 | ⚠️ 部分 | 核心框架已搭建，但关键功能未实现 |
| 安全性 | 🔴 严重 | 存在多处安全漏洞，必须修复 |
| 性能 | 🟡 需改进 | 存在潜在性能问题 |
| 可维护性 | 🟢 良好 | 代码结构清晰，命名规范 |
| 需求覆盖度 | 40% | 大量需求项未实现 |

---

## 🔴 阻塞级问题 (Blocking)

### 1. [安全] 下载/预览接口缺少权限校验
**文件**: `FileController.java:82-96`, `FileController.java:119-130`

```java
@GetMapping("/download/{fileId}")
public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) throws Exception {
    // ❌ 缺少：当前用户是否有权下载该文件的校验
    Path filePath = fileService.downloadFile(fileId);
    ...
}
```

**风险**: 任何用户只要知道 fileId 即可下载任意文件，违反 NF-102（文件访问需通过权限校验）。

**建议**: 添加 `@PreAuthorize` 或在 Service 层校验文件归属/权限。

---

### 2. [安全] 预览接口存在 XSS 风险
**文件**: `FileController.java:119-130`

```java
@GetMapping("/preview/{fileId}")
public ResponseEntity<Resource> previewFile(@PathVariable Long fileId) {
    // 直接返回文件内容，Content-Type 信任客户端输入
    String contentType = getContentType(fileInfo.getType());
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(contentType))
        .body(resource);
}
```

**风险**: 
- SVG 文件可嵌入恶意脚本，直接渲染会导致 XSS
- HTML/JS 文件预览将直接执行恶意代码

**建议**: 
- SVG/HTML 类型应使用沙箱 iframe 或转换为图片预览
- 添加 `Content-Security-Policy` 响应头

---

### 3. [安全] 分片上传缺少并发安全控制
**文件**: `FileController.java:42-49`

```java
@PostMapping("/upload/chunk")
public ResponseEntity<Void> uploadChunk(
        @RequestParam String uploadId,
        @RequestParam int chunkIndex,
        @RequestParam("chunk") MultipartFile chunk) {
    fileService.uploadChunk(uploadId, chunkIndex, chunk);
    // ❌ 缺少：并发分片写入的原子性保证
}
```

**风险**: 多个分片并发上传时可能导致文件损坏或数据竞争。

**建议**: 使用分布式锁或数据库乐观锁保证分片合并的原子性。

---

### 4. [安全] 文件类型校验不足
**文件**: `FileController.java:30-37`

```java
@PostMapping("/upload/init")
public ResponseEntity<Map<String, Object>> initUpload(
        @RequestParam String fileName,
        @RequestParam Long fileSize,
        @RequestParam String fileType, ...) {
    // ❌ 缺少：服务端对文件类型的白名单校验
    // ❌ 缺少：对文件内容的真实类型检测（Magic Number）
}
```

**风险**: 
- 客户端可伪造 fileType 绕过校验
- 可执行文件伪装成图片上传

**建议**: 
- 上传后读取文件头 Magic Number 验证真实类型
- 配置允许的文件类型白名单

---

## 🟡 重要问题 (Important)

### 5. [功能] 取消上传功能未实现
**文件**: `FileController.java:73-77`

```java
@PostMapping("/upload/cancel")
public ResponseEntity<Void> cancelUpload(@RequestParam String uploadId) {
    // TODO: 实现取消逻辑
    return ResponseEntity.ok().build();
}
```

**影响**: 无法满足 UP-007（取消上传任务）需求，断点续传状态无法主动清理。

---

### 6. [功能] 文件列表功能未实现
**文件**: `FileController.java:101-105`

```java
@GetMapping("/list")
public ResponseEntity<List<File>> listFiles() {
    // TODO: 添加分页、搜索、排序参数，暂时返回空列表
    return ResponseEntity.ok(java.util.Collections.emptyList());
}
```

**影响**: MG-001/002/003 需求未实现，用户无法管理已上传文件。

---

### 7. [性能] 文件预览未使用流式传输
**文件**: `FileController.java:124`

```java
Resource resource = new UrlResource(filePath.toUri());
// 直接加载整个文件到内存
```

**风险**: 大文件预览会占用大量内存，可能导致 OOM。

**建议**: 使用 `StreamingResponseBody` 或配置响应的 `Content-Length` 和 `Accept-Ranges` 支持断点播放。

---

### 8. [安全] 下载链接缺少签名和时效控制
**文件**: `FileController.java:82-96`

**需求**: DL-201/202/203 要求下载链接携带签名、有效期、次数限制。

**现状**: 直接暴露 `/download/{fileId}` 永久有效链接。

**建议**: 
- 生成带签名的临时下载 URL（如 `/download/{fileId}?token=xxx&expires=timestamp`）
- Token 签名校验逻辑

---

### 9. [安全] 上传接口缺少频率限制
**文件**: `FileController.java:30-37`, `FileController.java:42-49`

**需求**: NF-103 要求上传接口做频率限制防刷。

**现状**: 无任何限流措施。

**建议**: 添加 `@RateLimiter` 注解或使用 Bucket4j 限流。

---

### 10. [功能] 批量下载接口缺失
**需求**: DL-101/102/103 要求批量下载打包 ZIP。

**现状**: 仅实现单文件下载，无 `/download/batch` 接口。

---

## 🟢 改进建议 (Suggestion)

### 11. [设计] 文件名编码可简化
**文件**: `FileController.java:88-89`

```java
String encodedFileName = URLEncoder.encode(fileInfo.getName(), StandardCharsets.UTF_8)
    .replace("+", "%20");
```

**建议**: 使用 `ContentDisposition.attachment()` 工具类更规范：

```java
ContentDisposition disposition = ContentDisposition.attachment()
    .filename(fileInfo.getName(), StandardCharsets.UTF_8)
    .build();
return ResponseEntity.ok()
    .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
    .body(resource);
```

---

### 12. [设计] getContentType 方法可扩展
**文件**: `FileController.java:132-146`

**建议**: 
- 抽取为配置类或常量类，便于维护
- 添加更多 Office 文档类型支持（docx, xlsx, pptx）以满足 PV-003

---

### 13. [实体] File 实体字段建议
**文件**: `File.java`

```java
private String name;           // 原始文件名
private Long size;             // 文件大小（字节）
private String type;           // MIME 类型
```

**建议**:
- `name` 字段添加 `@Column(length = 255)` 和非空校验
- `size` 字段添加 `@Min(1)` 校验（UP-304 禁止空文件）
- 添加 `deleted` 软删除字段（满足 MG-005 删除后可恢复）
- 添加索引注解：`@Table(indexes = {@Index(name = "idx_hash", columnList = "hash")})`

---

### 14. [Repository] 建议添加更多查询方法
**文件**: `FileRepository.java`

```java
Optional<File> findByHash(String hash);
Optional<File> findByUploadId(String uploadId);
```

**建议添加**:
```java
Page<File> findByUploaderOrderByCreatedAtDesc(String uploader, Pageable pageable);
List<File> findByIdIn(List<Long> ids);  // 批量下载
void deleteByUploadIdAndStatus(String uploadId, FileStatus status);
```

---

## ✅ 良好实践

1. **架构分层清晰**: Controller → Service → Repository 分层合理
2. **使用了 Builder 模式**: File 实体使用 `@Builder` 便于构造
3. **分片上传设计**: 支持大文件分片，符合 UP-101 需求
4. **秒传机制**: `checkFile` 方法支持 UP-401/402/403 需求
5. **异常处理**: FileService 中使用 BusinessException 统一异常

---

## 📋 需求覆盖清单

| 需求编号 | 状态 | 说明 |
|----------|------|------|
| UP-001~007 | ⚠️ 部分 | 基础上传框架存在，但取消功能未实现 |
| UP-101~104 | ✅ 已实现 | 分片上传逻辑已实现 |
| UP-201~204 | ⚠️ 部分 | 断点续传需要持久化状态（7天过期未实现） |
| UP-301~304 | ❌ 未实现 | 文件校验逻辑缺失 |
| UP-401~403 | ✅ 已实现 | 秒传检查接口已实现 |
| DL-001~004 | ⚠️ 部分 | 基础下载存在，但无进度反馈 |
| DL-101~103 | ❌ 未实现 | 批量下载接口缺失 |
| DL-201~203 | ❌ 未实现 | 下载签名/时效控制缺失 |
| PV-001~006 | ⚠️ 部分 | 预览接口存在，但有 XSS 风险 |
| MG-001~007 | ❌ 未实现 | 文件列表/管理功能未实现 |
| NF-101 | ❌ 未实现 | 病毒扫描未集成 |
| NF-102 | ❌ 未实现 | 权限校验缺失 |
| NF-103 | ❌ 未实现 | 频率限制缺失 |
| NF-104 | ❌ 未实现 | 敏感文件加密存储未实现 |
| NF-105 | ❌ 未实现 | 审计日志未实现 |

---

## 🎯 评审结论

**🔄 需要修改 (Request Changes)**

### 优先级 P0（必须修复）:
1. 添加下载/预览接口的权限校验
2. 修复预览接口 XSS 风险
3. 实现文件类型服务端校验
4. 添加分片上传并发控制

### 优先级 P1（强烈建议）:
5. 实现取消上传功能
6. 实现文件列表管理功能
7. 添加下载链接签名和时效控制
8. 实现批量下载接口

### 优先级 P2（改进建议）:
9. 大文件预览使用流式传输
10. 添加上传频率限制
11. 完善实体字段校验和索引

---

**评审人**: Code Review Skill  
**评审时间**: 2026-07-15