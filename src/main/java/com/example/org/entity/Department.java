package com.example.org.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * 部门实体
 * 采用物化路径模式存储树形结构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {
    private Long id;
    private String name;
    private Long parentId;
    private String path;        // 物化路径: /1/2/3
    private Integer level;      // 层级深度
    private Integer sortOrder;
    private Integer status;     // 1-启用, 0-禁用
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}