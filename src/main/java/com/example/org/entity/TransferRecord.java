package com.example.org.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * 员工调动记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferRecord {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private Long fromDeptId;
    private Long toDeptId;
    private String fromPosition;
    private String toPosition;
    private Integer transferType;  // 1-部门调动, 2-职位变更, 3-部门+职位
    private String reason;
    private LocalDateTime transferDate;
    private LocalDateTime createdAt;
    private String createdBy;
}