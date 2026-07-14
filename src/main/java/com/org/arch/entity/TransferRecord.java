package com.org.arch.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 调动记录实体
 */
@Data
@TableName("sys_transfer_record")
public class TransferRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long employeeId;
    
    private String employeeName;
    
    private Long fromDeptId;
    
    private String fromDeptName;
    
    private Long toDeptId;
    
    private String toDeptName;
    
    private String fromPosition;
    
    private String toPosition;
    
    private String reason;
    
    private LocalDateTime transferDate;
    
    private Long operatorId;
}