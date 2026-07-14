# Task Plan: 组织架构与人员管理系统

## Overview
- **Proposal**: testdj-04-organization-management
- **Total Tasks**: 8
- **Estimated Effort**: 8-12 人日

---

## Phase 1: 数据库设计与基础设施

### Task 1.1: 创建数据库表结构
**Priority**: P0  
**Estimate**: 2h  
**Deliverables**:
- `V001__init_schema.sql` 包含:
  - `department` 表
  - `employee` 表
  - `transfer_record` 表
- 唯一索引: `employee_no`, `phone`
- 外键约束: `dept_id`, `parent_id`

**Validation**:
```bash
# 本地数据库执行迁移脚本
mysql -u root -p testdb < migration/V001__init_schema.sql
# 验证表结构
mysql -u root -p testdb -e "SHOW CREATE TABLE department;"
```

---

## Phase 2: 部门树功能

### Task 2.1: 部门树查询接口
**Priority**: P0  
**Estimate**: 4h  
**Deliverables**:
- `DepartmentController.getTree()`
- `DepartmentService.buildTree()`
- 单元测试: 递归构建正确性、空树处理

**API**:
```
GET /api/departments/tree
```

**Validation**:
```bash
curl http://localhost:8080/api/departments/tree
# 验证返回树形结构
```

### Task 2.2: 部门拖拽调整接口
**Priority**: P0  
**Estimate**: 4h  
**Deliverables**:
- `DepartmentController.move()`
- `DepartmentService.moveDepartment()`
- 循环引用校验逻辑
- 子部门 path/level 级联更新
- 单元测试: 循环检测、正常移动

**API**:
```
PUT /api/departments/{id}/move
```

**Validation**:
```bash
# 正常移动
curl -X PUT http://localhost:8080/api/departments/2/move \
  -H "Content-Type: application/json" \
  -d '{"newParentId": 5}'

# 循环引用校验（应返回400）
curl -X PUT http://localhost:8080/api/departments/1/move \
  -H "Content-Type: application/json" \
  -d '{"newParentId": 2}'
```

---

## Phase 3: 员工管理功能

### Task 3.1: 员工唯一性校验接口
**Priority**: P0  
**Estimate**: 2h  
**Deliverables**:
- `EmployeeController.checkUniqueness()`
- `EmployeeService.checkField()`
- 单元测试: 存在/不存在场景

**API**:
```
GET /api/employees/check?field=employeeNo&value=10086
```

### Task 3.2: 员工新增接口
**Priority**: P0  
**Estimate**: 3h  
**Deliverables**:
- `EmployeeController.create()`
- `EmployeeService.createEmployee()`
- 部门存在性校验
- 工号/手机号唯一性校验（二次确认）
- 单元测试: 正常创建、重复工号、部门不存在

**API**:
```
POST /api/employees
```

### Task 3.3: 员工调动接口
**Priority**: P0  
**Estimate**: 5h  
**Deliverables**:
- `EmployeeController.transfer()`
- `EmployeeService.transferEmployee()`
- `TransferRecordMapper.insert()`
- 事务控制: `@Transactional`
- 审批流更新逻辑（简化版：更新审批节点）
- 单元测试: 正常调动、目标部门不存在、事务回滚

**API**:
```
POST /api/employees/{id}/transfer
```

### Task 3.4: 员工离职接口
**Priority**: P0  
**Estimate**: 3h  
**Deliverables**:
- `EmployeeController.resign()`
- `EmployeeService.resignEmployee()`
- 逻辑删除实现（MyBatis-Plus `@TableLogic`）
- 权限释放逻辑（调用权限系统API）
- 单元测试: 离职后状态、权限清除

**API**:
```
PUT /api/employees/{id}/resign
GET /api/employees?status=ACTIVE
GET /api/employees?status=RESIGNED
```

---

## Phase 4: 集成测试与前端对接

### Task 4.1: 集成测试
**Priority**: P1  
**Estimate**: 4h  
**Deliverables**:
- 端到端测试场景:
  1. 创建部门树
  2. 拖拽调整部门
  3. 新增员工（含重复校验）
  4. 调动员工
  5. 办理离职
  6. 查询在职/离职员工
- Postman/Insomnia 测试集合

**Validation**:
```bash
newman run testdj-04-collection.postman_collection.json
```

---

## Dependency Graph

```
Task 1.1 (数据库)
  ├─> Task 2.1 (部门树查询)
  ├─> Task 2.2 (部门拖拽)
  ├─> Task 3.1 (唯一性校验)
  ├─> Task 3.2 (员工新增)
  ├─> Task 3.3 (员工调动) -> 依赖 Task 2.1
  └─> Task 3.4 (员工离职)
  
Task 4.1 (集成测试) -> 依赖所有后端任务
```

---

## Execution Order

1. **Day 1**: Task 1.1 + Task 2.1
2. **Day 2**: Task 2.2 + Task 3.1
3. **Day 3**: Task 3.2 + Task 3.3
4. **Day 4**: Task 3.4 + Task 4.1

---

## Notes

- 所有任务优先实现后端 API，前端对接可并行
- 审批流更新逻辑先实现简化版（硬编码），后续迭代配置化
- 权限释放逻辑需与权限系统对接，建议先 Mock
- 集成测试可使用 H2 内存数据库加速