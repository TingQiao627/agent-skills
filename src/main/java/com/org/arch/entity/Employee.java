package com.org.arch.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工实体
 */
@Data
@TableName("sys_employee")
public class Employee {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String employeeNo;
    
    private String phone;
    
    private Long deptId;
    
    private String position;
    
    private String email;
    
    /** 状态：1-在职，0-离职 */
    private Integer status;
    
    private LocalDate hireDate;
    
    private LocalDate resignDate;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}