package com.org.arch.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 部门实体
 */
@Data
@TableName("sys_department")
public class Department {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private Long parentId;
    
    private Integer level;
    
    private String path;
    
    private Integer sortOrder;
    
    private Long leaderId;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    /** 子部门列表（非数据库字段） */
    @TableField(exist = false)
    private List<Department> children = new ArrayList<>();
}