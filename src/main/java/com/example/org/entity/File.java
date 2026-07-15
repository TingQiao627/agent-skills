package com.example.org.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 文件实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "sys_file")
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;           // 原始文件名
    private Long size;             // 文件大小（字节）
    private String type;           // MIME 类型
    private String hash;           // SHA256 哈希值
    private String storageKey;     // 存储路径/Key
    
    @Enumerated(EnumType.STRING)
    private FileStatus status;     // 文件状态
    
    private String uploader;       // 上传者
    private String uploadId;       // 分片上传ID
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public enum FileStatus {
        UPLOADING, COMPLETED, FAILED
    }
}