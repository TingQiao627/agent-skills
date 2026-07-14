package com.example.org.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    private Long id;
    private String name;
    private String employeeNo;  // 工号
    private String phone;
    private Long deptId;
    private String position;
    private Integer status;     // 1-在职, 0-离职
    private String email;
    private LocalDate hireDate;
    private LocalDate resignDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}