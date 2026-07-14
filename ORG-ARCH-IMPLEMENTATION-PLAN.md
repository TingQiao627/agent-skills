# 组织架构与人员管理系统 - 实施计划

> **Author:** AI Planning Agent  
> **Date:** 2026-07-14  
> **Status:** Ready for Implementation  
> **Technology Stack:** Spring Boot 3.x + MyBatis Plus + MySQL 8.0 + Vue 3 (推荐)

---

## 一、技术选型依据

| 层级 | 技术栈 | 理由 |
|------|--------|------|
| 后端框架 | Spring Boot 3.x | 企业级标准，生态成熟，事务管理完善 |
| 数据访问 | MyBatis Plus | 提供强大的树形结构查询支持，自动分页 |
| 数据库 | MySQL 8.0 | 支持递归查询（CTE），适合部门树 |
| 前端框架 | Vue 3 + Element Plus | 树形组件成熟，拖拽交互支持好 |
| API规范 | RESTful + 统一响应体 | 前后端分离标准 |

---

## 二、数据库设计

### 2.1 部门表 (sys_department)

```sql
CREATE TABLE sys_department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '部门ID',
    name VARCHAR(100) NOT NULL COMMENT '部门名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID，0表示根节点',
    level INT NOT NULL DEFAULT 1 COMMENT '层级深度',
    path VARCHAR(500) COMMENT '路径ID链，如 1/3/7，便于查询所有祖先',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
    leader_id BIGINT COMMENT '部门主管员工ID',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id),
    INDEX idx_path (path(191))
) ENGINE=InnoDB COMMENT='部门表';
```

### 2.2 员工表 (sys_employee)

```sql
CREATE TABLE sys_employee (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '员工ID',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    employee_no VARCHAR(20) NOT NULL COMMENT '工号',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    dept_id BIGINT NOT NULL COMMENT '所属部门ID',
    position VARCHAR(100) COMMENT '职位',
    email VARCHAR(100) COMMENT '邮箱',
    status TINYINT DEFAULT 1 COMMENT '状态：1-在职，0-离职',
    hire_date DATE COMMENT '入职日期',
    resign_date DATE COMMENT '离职日期',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_employee_no (employee_no),
    UNIQUE KEY uk_phone (phone),
    INDEX idx_dept_id (dept_id),
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='员工表';
```

### 2.3 调动记录表 (sys_transfer_record)

```sql
CREATE TABLE sys_transfer_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    employee_name VARCHAR(50) COMMENT '员工姓名（冗余）',
    from_dept_id BIGINT NOT NULL COMMENT '原部门ID',
    from_dept_name VARCHAR(100) COMMENT '原部门名称（冗余）',
    to_dept_id BIGINT NOT NULL COMMENT '目标部门ID',
    to_dept_name VARCHAR(100) COMMENT '目标部门名称（冗余）',
    from_position VARCHAR(100) COMMENT '原职位',
    to_position VARCHAR(100) COMMENT '新职位',
    reason VARCHAR(500) COMMENT '调动原因',
    transfer_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '调动时间',
    operator_id BIGINT COMMENT '操作人ID',
    INDEX idx_employee_id (employee_id)
) ENGINE=InnoDB COMMENT='调动记录表';
```

---

## 三、任务分解

### Task 1: 项目初始化与基础配置

**Files:**
- Create: `pom.xml`
- Create: `src/main/resources/application.yml`
- Create: `src/main/java/com/org/arch/OrgArchApplication.java`
- Create: `src/main/java/com/org/arch/common/Result.java`

**Interfaces:**
- Produces: 统一响应体 `Result<T>`，包含 code、msg、data

**Steps:**
- [ ] 创建 Spring Boot 项目，引入 Web、MyBatis Plus、MySQL 驱动依赖
- [ ] 配置数据库连接、MyBatis Plus 分页插件
- [ ] 创建启动类和统一响应体

---

### Task 2: 部门实体与 Mapper

**Files:**
- Create: `src/main/java/com/org/arch/entity/Department.java`
- Create: `src/main/java/com/org/arch/mapper/DepartmentMapper.java`
- Test: `src/test/java/com/org/arch/mapper/DepartmentMapperTest.java`

**Interfaces:**
- Produces: `Department` 实体，包含 children 字段用于树形结构

**Steps:**
- [ ] 编写部门实体类，使用 MyBatis Plus 注解
- [ ] 创建 Mapper 接口，继承 BaseMapper
- [ ] 编写单元测试验证基本 CRUD

---

### Task 3: 部门树形结构查询 API

**Files:**
- Create: `src/main/java/com/org/arch/service/DepartmentService.java`
- Create: `src/main/java/com/org/arch/controller/DepartmentController.java`
- Test: `src/test/java/com/org/arch/controller/DepartmentControllerTest.java`

**Interfaces:**
- Produces: `GET /api/departments/tree` - 返回完整部门树
- Produces: `GET /api/departments/{id}/children` - 懒加载子部门

**Steps:**
- [ ] 实现递归查询部门树方法（使用 MySQL 8.0 CTE 或应用层递归）
- [ ] 实现 Controller 提供树形接口
- [ ] 编写集成测试验证树形结构正确性

---

### Task 4: 部门拖拽移动 API

**Files:**
- Modify: `src/main/java/com/org/arch/service/DepartmentService.java`
- Modify: `src/main/java/com/org/arch/controller/DepartmentController.java`
- Test: `src/test/java/com/org/arch/service/DepartmentServiceTest.java`

**Interfaces:**
- Consumes: 部门树查询结果
- Produces: `PUT /api/departments/{id}/move` - 更新父节点

**Steps:**
- [ ] 编写测试用例：移动部门后，path 和 level 应正确更新
- [ ] 实现移动逻辑：更新 parent_id、重新计算 path 和 level
- [ ] 添加事务注解确保数据一致性
- [ ] 验证：不能移动到自己或自己的子节点下

---

### Task 5: 员工实体与唯一性校验 API

**Files:**
- Create: `src/main/java/com/org/arch/entity/Employee.java`
- Create: `src/main/java/com/org/arch/mapper/EmployeeMapper.java`
- Create: `src/main/java/com/org/arch/service/EmployeeService.java`
- Create: `src/main/java/com/org/arch/controller/EmployeeController.java`
- Test: `src/test/java/com/org/arch/service/EmployeeServiceTest.java`

**Interfaces:**
- Produces: `GET /api/employees/check?field=employeeNo&value=10086`
- Produces: `{ "code": 200, "data": { "isExist": false } }`

**Steps:**
- [ ] 创建员工实体，字段与数据库对齐
- [ ] 实现唯一性校验方法：查询工号和手机号是否已存在
- [ ] Controller 提供实时校验接口
- [ ] 测试：重复工号返回 isExist=true

---

### Task 6: 员工新增 API

**Files:**
- Modify: `src/main/java/com/org/arch/service/EmployeeService.java`
- Modify: `src/main/java/com/org/arch/controller/EmployeeController.java`
- Test: `src/test/java/com/org/arch/controller/EmployeeControllerTest.java`

**Interfaces:**
- Consumes: 唯一性校验结果
- Produces: `POST /api/employees` - 创建员工

**Steps:**
- [ ] 编写测试：部门不存在时应抛出异常
- [ ] 实现新增逻辑：校验部门存在性、再次校验工号和手机号唯一性
- [ ] 保存员工记录，设置初始状态为在职
- [ ] 集成测试验证完整流程

---

### Task 7: 员工查询与列表 API

**Files:**
- Modify: `src/main/java/com/org/arch/service/EmployeeService.java`
- Modify: `src/main/java/com/org/arch/controller/EmployeeController.java`

**Interfaces:**
- Produces: `GET /api/employees?deptId=2&status=1&page=1&size=10`
- Produces: 分页响应，包含总条数和列表

**Steps:**
- [ ] 实现分页查询方法，支持部门筛选和状态筛选
- [ ] Controller 提供列表接口
- [ ] 测试分页逻辑正确性

---

### Task 8: 员工调动 API 与级联处理

**Files:**
- Modify: `src/main/java/com/org/arch/service/EmployeeService.java`
- Create: `src/main/java/com/org/arch/entity/TransferRecord.java`
- Create: `src/main/java/com/org/arch/mapper/TransferRecordMapper.java`
- Test: `src/test/java/com/org/arch/service/EmployeeTransferTest.java`

**Interfaces:**
- Consumes: 员工信息
- Produces: `POST /api/employees/{id}/transfer`
- Produces: 调动记录实体

**Steps:**
- [ ] 编写测试：调动后员工 dept_id 应更新
- [ ] 实现调动逻辑：更新员工 dept_id 和 position
- [ ] 插入调动记录表，记录原部门、新部门、原因
- [ ] 标记：预留审批流/权限更新接口调用点（后续系统集成）
- [ ] 验证事务完整性

---

### Task 9: 员工离职 API（逻辑删除）

**Files:**
- Modify: `src/main/java/com/org/arch/service/EmployeeService.java`
- Modify: `src/main/java/com/org/arch/controller/EmployeeController.java`
- Test: `src/test/java/com/org/arch/service/EmployeeResignTest.java`

**Interfaces:**
- Consumes: 员工状态
- Produces: `PUT /api/employees/{id}/resign`

**Steps:**
- [ ] 编写测试：离职后 status=0，resign_date 有值
- [ ] 实现离职逻辑：更新状态为离职，设置离职日期
- [ ] 标记：预留系统账号许可释放接口调用点
- [ ] 验证历史数据查询仍可返回离职员工

---

### Task 10: 权限控制与角色隔离

**Files:**
- Create: `src/main/java/com/org/arch/security/PermissionAspect.java`
- Create: `src/main/java/com/org/arch/annotation/RequireRole.java`

**Interfaces:**
- Consumes: 当前登录用户信息（从 SecurityContext 获取）
- Produces: 权限校验切面

**Steps:**
- [ ] 定义角色枚举：SUPER_ADMIN, HR, DEPT_MANAGER
- [ ] 实现权限切面：超管/HR 可操作所有，部门主管仅可操作本部门及下属部门
- [ ] 在 Controller 方法上添加权限注解
- [ ] 测试权限隔离正确性

---

## 四、依赖关系图

```
Task 1 (项目初始化)
   ↓
Task 2 (部门实体) ─────┐
   ↓                   │
Task 3 (部门树查询)    │
   ↓                   │
Task 4 (部门拖拽)      │
                        │
Task 5 (员工实体+校验) ←┘
   ↓
Task 6 (员工新增)
   ↓
Task 7 (员工列表查询)
   ↓
Task 8 (员工调动)
   ↓
Task 9 (员工离职)
   ↓
Task 10 (权限控制) ──→ 横切所有 API
```

---

## 五、验证检查清单

### 功能验证
- [ ] 部门树可正确加载，懒加载子部门正常
- [ ] 部门拖拽后，path 和 level 自动更新
- [ ] 工号和手机号实时校验返回正确结果
- [ ] 员工新增时唯一性校验生效，部门存在性校验生效
- [ ] 员工调动后部门变更，调动记录正确写入
- [ ] 员工离职后状态正确，历史数据仍可查询
- [ ] 权限控制：部门主管无法编辑其他部门员工

### 数据一致性
- [ ] 拖拽操作使用事务，失败时回滚
- [ ] 调动操作使用事务，员工表和调动记录表同步更新

### 性能考虑
- [ ] 部门树查询使用索引优化
- [ ] 员工列表分页避免全表扫描

---

## 六、风险与回退策略

| 风险点 | 影响 | 缓解措施 |
|--------|------|----------|
| 递归查询性能问题（部门树过大） | 页面加载慢 | 限制树深度，采用懒加载；考虑使用物化路径 |
| 并发调动同一员工 | 数据不一致 | 使用乐观锁（version 字段）或分布式锁 |
| 审批流/权限系统集成延迟 | 调动后权限未同步 | 提供手动触发接口，记录待同步队列 |

---

## 七、后续集成接口预留

为便于其他业务系统集成，建议预留以下接口：

1. **审批流系统调用**：`POST /internal/employees/{id}/sync-approval-flow`
2. **权限系统调用**：`POST /internal/employees/{id}/sync-permissions`
3. **账号许可释放**：`DELETE /internal/employees/{id}/license`

---

## 八、执行时间估算

| 任务 | 预计时间 |
|------|----------|
| Task 1-2 | 2小时 |
| Task 3-4 | 3小时 |
| Task 5-7 | 3小时 |
| Task 8-9 | 2小时 |
| Task 10 | 1.5小时 |
| 集成测试 | 1.5小时 |
| **总计** | **13小时** |

---

**计划制定完成，可直接进入实施阶段。**