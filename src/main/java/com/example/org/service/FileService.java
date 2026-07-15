package com.example.org.service;

import com.example.org.entity.File;
import com.example.org.entity.File.FileStatus;
import com.example.org.exception.BusinessException;
import com.example.org.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {
    
    private final FileRepository fileRepository;
    
    // 配置项
    private static final long MAX_FILE_SIZE = 2L * 1024 * 1024 * 1024; // 2GB
    private static final long CHUNK_SIZE = 5L * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_TYPES = Set.of(
        "jpg", "jpeg", "png", "gif", "webp", "svg",
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
        "txt", "mp4", "mp3"
    );
    
    private final Path storagePath = Paths.get("./uploads");
    
    /**
     * 初始化上传
     */
    @Transactional
    public Map<String, Object> initUpload(String fileName, Long fileSize, String fileType, String uploader) {
        // 校验文件大小
        if (fileSize > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小超过限制");
        }
        
        // 校验文件类型
        String ext = getFileExtension(fileName);
        if (!ALLOWED_TYPES.contains(ext.toLowerCase())) {
            throw new BusinessException("不支持的文件类型: " + ext);
        }
        
        if (fileSize == 0) {
            throw new BusinessException("禁止上传空文件");
        }
        
        String uploadId = UUID.randomUUID().toString();
        
        File file = File.builder()
            .name(fileName)
            .size(fileSize)
            .type(fileType)
            .uploadId(uploadId)
            .status(FileStatus.UPLOADING)
            .uploader(uploader)
            .createdAt(LocalDateTime.now())
            .build();
        
        fileRepository.save(file);
        
        int chunkCount = (int) Math.ceil((double) fileSize / CHUNK_SIZE);
        
        return Map.of(
            "uploadId", uploadId,
            "chunkSize", CHUNK_SIZE,
            "chunkCount", chunkCount
        );
    }
    
    /**
     * 上传分片
     */
    @Transactional
    public void uploadChunk(String uploadId, int chunkIndex, MultipartFile chunkFile) {
        File file = fileRepository.findByUploadId(uploadId)
            .orElseThrow(() -> new BusinessException("上传任务不存在"));
        
        try {
            Path chunkDir = storagePath.resolve(uploadId);
            Files.createDirectories(chunkDir);
            
            Path chunkPath = chunkDir.resolve(chunkIndex + ".part");
            chunkFile.transferTo(chunkPath);
            
            log.info("分片上传成功: uploadId={}, chunkIndex={}", uploadId, chunkIndex);
        } catch (IOException e) {
            log.error("分片上传失败", e);
            throw new BusinessException("分片上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 合并分片
     */
    @Transactional
    public Long mergeChunks(String uploadId, String hash) {
        File file = fileRepository.findByUploadId(uploadId)
            .orElseThrow(() -> new BusinessException("上传任务不存在"));
        
        try {
            Path chunkDir = storagePath.resolve(uploadId);
            String storageKey = UUID.randomUUID().toString() + "_" + file.getName();
            Path targetPath = storagePath.resolve(storageKey);
            
            Files.createDirectories(storagePath);
            
            // 合并分片
            int chunkCount = (int) Math.ceil((double) file.getSize() / CHUNK_SIZE);
            try (var outputStream = Files.newOutputStream(targetPath)) {
                for (int i = 0; i < chunkCount; i++) {
                    Path chunkPath = chunkDir.resolve(i + ".part");
                    Files.copy(chunkPath, outputStream);
                }
            }
            
            // 计算哈希验证
            String actualHash = calculateHash(targetPath);
            if (!actualHash.equals(hash)) {
                Files.delete(targetPath);
                throw new BusinessException("文件完整性校验失败");
            }
            
            // 清理临时分片
            Files.walk(chunkDir)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try { Files.delete(path); } catch (IOException ignored) {}
                });
            
            // 更新文件记录
            file.setStorageKey(storageKey);
            file.setHash(hash);
            file.setStatus(FileStatus.COMPLETED);
            file.setUpdatedAt(LocalDateTime.now());
            fileRepository.save(file);
            
            log.info("文件合并成功: fileId={}", file.getId());
            return file.getId();
        } catch (IOException e) {
            log.error("合并分片失败", e);
            throw new BusinessException("合并分片失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查文件是否存在（秒传）
     */
    public Map<String, Object> checkFile(String hash) {
        Optional<File> existingFile = fileRepository.findByHash(hash);
        
        if (existingFile.isPresent()) {
            File file = existingFile.get();
            return Map.of(
                "exists", true,
                "fileId", file.getId(),
                "name", file.getName(),
                "url", "/api/files/download/" + file.getId()
            );
        }
        
        return Map.of("exists", false);
    }
    
    /**
     * 下载文件
     */
    public Path downloadFile(Long fileId) {
        File file = fileRepository.findById(fileId)
            .orElseThrow(() -> new BusinessException("文件不存在"));
        
        if (file.getStatus() != FileStatus.COMPLETED) {
            throw new BusinessException("文件未完成上传");
        }
        
        return storagePath.resolve(file.getStorageKey());
    }
    
    /**
     * 获取文件信息
     */
    public File getFileInfo(Long fileId) {
        return fileRepository.findById(fileId)
            .orElseThrow(() -> new BusinessException("文件不存在"));
    }
    
    /**
     * 删除文件
     */
    @Transactional
    public void deleteFile(Long fileId) {
        File file = fileRepository.findById(fileId)
            .orElseThrow(() -> new BusinessException("文件不存在"));
        
        // 删除物理文件
        try {
            if (file.getStorageKey() != null) {
                Files.deleteIfExists(storagePath.resolve(file.getStorageKey()));
            }
        } catch (IOException e) {
            log.warn("删除物理文件失败: {}", file.getStorageKey(), e);
        }
        
        fileRepository.delete(file);
        log.info("文件已删除: fileId={}", fileId);
    }
    
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(dotIndex + 1) : "";
    }
    
    private String calculateHash(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] fileBytes = Files.readAllBytes(file);
        byte[] hashBytes = digest.digest(fileBytes);
        
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}