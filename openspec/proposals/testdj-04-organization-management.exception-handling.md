# Exception Handling Strategy: 组织架构与人员管理系统

## Overview
本文档定义组织架构管理系统的异常兜底策略，确保核心业务场景的故障可恢复、数据不丢失、用户体验可控。

---

## 1. 全局异常处理框架

### 1.1 异常分层设计
```
BusinessException (业务异常)
├── DeptNotFoundException
├── EmployeeNotFoundException
├── DuplicateEmployeeNoException
├── DuplicatePhoneException
├── CircularDeptReferenceException
├── DeptNotEmptyException
├── TransferFailedException
└── ResignFailedException

SystemException (系统异常)
├── DatabaseConnectionException
├── ExternalServiceException
└── TimeoutException
```

### 1.2 全局异常处理器
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusiness(BusinessException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(e.getCode(), e.getMessage()));
    }
    
    @ExceptionHandler(SystemException.class)
    public ResponseEntity<ApiResponse<?>> handleSystem(SystemException e) {
        log.error("系统异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(500, "系统繁忙，请稍后重试"));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleUnknown(Exception e) {
        log.error("未知异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(500, "系统错误"));
    }
}
```

---

## 2. 核心场景异常兜底

### 2.1 部门拖拽异常

#### 异常场景
| 场景 | 原因 | 兜底策略 |
|------|------|----------|
| 循环引用 | 目标节点是当前节点的子孙 | 后端校验拒绝 + 前端预校验拦截 |
| 部门不存在 | 目标部门被删除 | 查询时返回 404 + 提示"目标部门不存在" |
| 数据库更新失败 | 网络中断、死锁 | 事务自动回滚 + 返回"操作失败，请重试" |

#### 兜底逻辑
```java
@Transactional
public void moveDepartment(Long deptId, Long newParentId) {
    // 1. 循环引用校验
    if (isDescendant(newParentId, deptId)) {
        throw new CircularDeptReferenceException("不能将部门移动到子部门下");
    }
    
    // 2. 目标部门存在性校验
    Department parent = deptMapper.selectById(newParentId);
    if (parent == null) {
        throw new DeptNotFoundException("目标部门不存在");
    }
    
    // 3. 更新操作（事务保护）
    updatePathAndLevel(deptId, newParentId);
}
```

---

### 2.2 部门删除异常

#### 异常场景
| 场景 | 原因 | 兜底策略 |
|------|------|----------|
| 部门下有员工 | 员工未清空或转移 | 返回 40005 + 提示"该部门下仍有员工，无法删除" |
| 部门不存在 | 部门已被删除 | 返回 404 + 提示"部门不存在" |
| 部门有子部门 | 子部门未清空 | 返回 40006 + 提示"该部门有子部门，无法删除" |

#### 兜底逻辑
```java
public void deleteDepartment(Long deptId) {
    // 1. 检查部门是否存在
    Department dept = deptMapper.selectById(deptId);
    if (dept == null) {
        throw new DeptNotFoundException("部门不存在");
    }
    
    // 2. 检查部门下是否有员工
    Integer empCount = employeeMapper.countByDeptId(deptId);
    if (empCount > 0) {
        throw new DeptNotEmptyException("该部门下仍有员工，无法删除");
    }
    
    // 3. 检查是否有子部门
    Integer childCount = deptMapper.countByParentId(deptId);
    if (childCount > 0) {
        throw new BusinessException(40006, "该部门有子部门，无法删除");
    }
    
    // 4. 执行删除
    deptMapper.deleteById(deptId);
}
```

---

### 2.3 唯一性校验异常

#### 异常场景
| 场景 | 原因 | 兜底策略 |
|------|------|----------|
| 工号重复 | 高并发插入相同工号 | 数据库唯一索引拦截 + 返回"工号已存在" |
| 手机号重复 | 高并发插入相同手机号 | 数据库唯一索引拦截 + 返回"手机号已注册" |
| 前端校验后仍重复 | 校验与提交间时序窗口 | 后端二次校验 + 分布式锁（可选） |

#### 兜底逻辑
```java
public Long createEmployee(EmployeeDTO dto) {
    // 1. 后端二次校验
    if (employeeMapper.existsByEmployeeNo(dto.getEmployeeNo())) {
        throw new DuplicateEmployeeNoException("工号已存在");
    }
    if (employeeMapper.existsByPhone(dto.getPhone())) {
        throw new DuplicatePhoneException("手机号已注册");
    }
    
    // 2. 插入（唯一索引兜底）
    try {
        Employee emp = convert(dto);
        employeeMapper.insert(emp);
        return emp.getId();
    } catch (DuplicateKeyException e) {
        throw new BusinessException("工号或手机号已存在，请确认后重试");
    }
}
```

---

### 2.3 员工调动异常

#### 异常场景
| 场景 | 原因 | 兜底策略 |
|------|------|----------|
| 目标部门不存在 | 部门被删除 | 返回 404 + 提示"目标部门不存在" |
| 审批流更新失败 | 审批系统异常 | 事务回滚 + 返回"调动失败，请重试" |
| 调动记录插入失败 | 数据库异常 | 事务回滚 + 返回"调动失败，请重试" |

#### 兜底逻辑
```java
@Transactional(rollbackFor = Exception.class)
public void transferEmployee(Long empId, TransferDTO dto) {
    Employee emp = employeeMapper.selectById(empId);
    if (emp == null) {
        throw new EmployeeNotFoundException("员工不存在");
    }
    
    Department newDept = deptMapper.selectById(dto.getNewDeptId());
    if (newDept == null) {
        throw new DeptNotFoundException("目标部门不存在");
    }
    
    // 更新员工部门
    employeeMapper.updateDept(empId, dto.getNewDeptId(), dto.getNewPosition());
    
    // 更新审批流（可降级）
    try {
        workflowService.updateApprovalNodes(empId, dto.getNewDeptId());
    } catch (Exception e) {
        log.warn("审批流更新失败，记录待补偿: empId={}", empId, e);
        // 记录补偿任务
        compensationTaskMapper.insert(new CompensationTask(empId, "TRANSFER"));
    }
    
    // 记录调动历史
    transferRecordMapper.insert(buildRecord(emp, dto));
}
```

---

### 2.4 员工离职异常

#### 异常场景
| 场景 | 原因 | 兜底策略 |
|------|------|----------|
| 员工不存在 | 员工已被删除 | 返回 404 + 提示"员工不存在" |
| 权限系统异常 | 权限服务不可用 | 降级处理：标记待释放权限 + 定时补偿 |
| 数据库更新失败 | 网络中断 | 事务回滚 + 返回"离职办理失败，请重试" |

#### 兜底逻辑
```java
@Transactional(rollbackFor = Exception.class)
public void resignEmployee(Long empId, LocalDate resignDate) {
    Employee emp = employeeMapper.selectById(empId);
    if (emp == null) {
        throw new EmployeeNotFoundException("员工不存在");
    }
    
    // 更新员工状态
    emp.setStatus("RESIGNED");
    emp.setResignDate(resignDate);
    emp.setResignedAt(LocalDateTime.now());
    employeeMapper.updateById(emp);
    
    // 释放权限（降级处理）
    try {
        permissionService.releasePermission(empId);
    } catch (Exception e) {
        log.warn("权限释放失败，记录待补偿: empId={}", empId, e);
        // 记录补偿任务
        compensationTaskMapper.insert(new CompensationTask(empId, "RELEASE_PERMISSION"));
    }
}
```

---

## 3. 补偿机制设计

### 3.1 补偿任务表
```sql
CREATE TABLE compensation_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  biz_type VARCHAR(50) NOT NULL COMMENT 'TRANSFER, RELEASE_PERMISSION',
  biz_id BIGINT NOT NULL COMMENT '业务ID',
  status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING, SUCCESS, FAILED',
  retry_count INT DEFAULT 0,
  max_retry INT DEFAULT 3,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  executed_at TIMESTAMP,
  INDEX idx_status (status),
  INDEX idx_biz (biz_type, biz_id)
);
```

### 3.2 定时补偿任务
```java
@Scheduled(fixedDelay = 300000) // 5分钟
public void compensatePendingTasks() {
    List<CompensationTask> tasks = compensationTaskMapper.selectPending(100);
    for (CompensationTask task : tasks) {
        try {
            executeCompensation(task);
            task.setStatus("SUCCESS");
        } catch (Exception e) {
            task.setRetryCount(task.getRetryCount() + 1);
            if (task.getRetryCount() >= task.getMaxRetry()) {
                task.setStatus("FAILED");
                alertService.notify("补偿任务失败: " + task.getId());
            }
        }
        compensationTaskMapper.updateById(task);
    }
}
```

---

## 4. 前端异常提示规范

### 4.1 统一错误码映射
| Code | 含义 | 用户提示 |
|------|------|----------|
| 40001 | 工号重复 | "工号已存在，请修改后重试" |
| 40002 | 手机号重复 | "手机号已注册，请确认或更换" |
| 40003 | 部门不存在 | "所选部门不存在，请刷新后重试" |
| 40004 | 循环引用 | "不能将部门移动到子部门下" |
| 40005 | 部门有员工 | "该部门下仍有员工，无法删除" |
| 40006 | 部门有子部门 | "该部门有子部门，无法删除" |
| 40401 | 员工不存在 | "员工信息不存在，请刷新列表" |
| 50000 | 系统异常 | "系统繁忙，请稍后重试" |

### 4.2 前端统一处理
```typescript
// axios 响应拦截器
axios.interceptors.response.use(
  response => response.data,
  error => {
    const { code, msg } = error.response?.data || {};
    switch (code) {
      case 40001:
      case 40002:
        message.error(msg);
        break;
      case 40401:
        message.warning(msg);
        router.push('/employees');
        break;
      default:
        message.error('系统异常，请稍后重试');
    }
    return Promise.reject(error);
  }
);
```

---

## 5. 监控与告警

### 5.1 关键指标监控
- 补偿任务堆积数量
- 事务回滚率
- 外部服务调用失败率
- 接口 5xx 错误率

### 5.2 告警规则
- 补偿任务 > 100 条：P2 告警
- 事务回滚率 > 5%：P2 告警
- 权限服务调用失败率 > 10%：P1 告警

---

## References
- Spring 事务管理: https://spring.io/guides/gs/managing-transactions/
- 分布式事务补偿模式: https://martinfowler.com/articles/patterns-of-distributed-systems/saga.html