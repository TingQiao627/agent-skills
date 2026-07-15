package com.example.org.controller;

import com.example.org.entity.File;
import com.example.org.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    
    private final FileService fileService;
    
    /**
     * 初始化上传
     */
    @PostMapping("/upload/init")
    public ResponseEntity<Map<String, Object>> initUpload(
            @RequestParam String fileName,
            @RequestParam Long fileSize,
            @RequestParam String fileType,
            @RequestParam(defaultValue = "anonymous") String uploader) {
        return ResponseEntity.ok(fileService.initUpload(fileName, fileSize, fileType, uploader));
    }
    
    /**
     * 上传分片
     */
    @PostMapping("/upload/chunk")
    public ResponseEntity<Void> uploadChunk(
            @RequestParam String uploadId,
            @RequestParam int chunkIndex,
            @RequestParam("chunk") MultipartFile chunk) {
        fileService.uploadChunk(uploadId, chunkIndex, chunk);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 合并分片
     */
    @PostMapping("/upload/merge")
    public ResponseEntity<Map<String, Object>> mergeChunks(
            @RequestParam String uploadId,
            @RequestParam String hash) {
        Long fileId = fileService.mergeChunks(uploadId, hash);
        return ResponseEntity.ok(Map.of("fileId", fileId, "message", "上传成功"));
    }
    
    /**
     * 检查文件是否存在（秒传）
     */
    @PostMapping("/upload/check")
    public ResponseEntity<Map<String, Object>> checkFile(@RequestParam String hash) {
        return ResponseEntity.ok(fileService.checkFile(hash));
    }
    
    /**
     * 取消上传
     */
    @PostMapping("/upload/cancel")
    public ResponseEntity<Void> cancelUpload(@RequestParam String uploadId) {
        // TODO: 实现取消逻辑
        return ResponseEntity.ok().build();
    }
    
    /**
     * 下载文件
     */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) throws Exception {
        Path filePath = fileService.downloadFile(fileId);
        File fileInfo = fileService.getFileInfo(fileId);
        
        Resource resource = new UrlResource(filePath.toUri());
        String encodedFileName = URLEncoder.encode(fileInfo.getName(), StandardCharsets.UTF_8)
            .replace("+", "%20");
        
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename*=UTF-8''" + encodedFileName)
            .body(resource);
    }
    
    /**
     * 文件列表
     */
    @GetMapping("/list")
    public ResponseEntity<List<File>> listFiles() {
        // TODO: 添加分页、搜索、排序参数，暂时返回空列表
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }
    
    /**
     * 删除文件
     */
    @PostMapping("/delete")
    public ResponseEntity<Void> deleteFile(@RequestParam Long fileId) {
        fileService.deleteFile(fileId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 文件预览
     */
    @GetMapping("/preview/{fileId}")
    public ResponseEntity<Resource> previewFile(@PathVariable Long fileId) throws Exception {
        Path filePath = fileService.downloadFile(fileId);
        File fileInfo = fileService.getFileInfo(fileId);
        
        Resource resource = new UrlResource(filePath.toUri());
        String contentType = getContentType(fileInfo.getType());
        
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .body(resource);
    }
    
    private String getContentType(String fileType) {
        if (fileType == null) return "application/octet-stream";
        
        return switch (fileType.toLowerCase()) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "svg" -> "image/svg+xml";
            case "pdf" -> "application/pdf";
            case "mp4" -> "video/mp4";
            case "mp3" -> "audio/mpeg";
            default -> "application/octet-stream";
        };
    }
}