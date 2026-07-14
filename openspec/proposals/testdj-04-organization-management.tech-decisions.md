# Technical Decisions: 组织架构与人员管理系统

## Decision Log

### TD-01: 部门树存储方案
**Decision**: 采用 **物化路径（Materialized Path）** 模式  
**Alternatives Considered**:
- 邻接表（Adjacency List）: 查询子树需要递归，性能差
- 嵌套集（Nested Set）: 插入/更新成本高
- 闭包表（Closure Table）: 需要额外关系表，维护复杂

**Rationale**: 物化路径平衡了查询性能与维护成本：
- 查询子树: `WHERE path LIKE '1.2.%'`
- 更新路径: 拖拽时批量更新子节点路径
- 支持懒加载: 按 `parentId` 查询单层子节点

**Trade-offs**:
- ✅ 查询子树 O(1) 次数据库访问
- ✅ 层级深度无限制
- ❌ 拖拽需批量更新子节点路径

---

### TD-02: 唯一性校验策略
**Decision**: **前端防抖 + 后端双重校验**  
**Implementation**:
1. 前端输入框 `blur` 事件触发，防抖 300ms
2. 后端接收校验请求，查询数据库
3. 提交时再次校验（数据库唯一索引兜底）

**Rationale**:
- 前端防抖减少不必要的请求
- 数据库唯一索引是最终防线，防止并发写入

**Trade-offs**:
- ✅ 用户体验好（实时反馈）
- ✅ 数据一致性有保障
- ❌ 高并发场景下仍有极小概率重复（需分布式锁）

---

### TD-03: 调动事务管理
**Decision**: **Spring 声明式事务 + 补偿机制**  
**Implementation**:
```java
@Transactional
public void transferEmployee(Long empId, Long newDeptId, String newPosition, String reason) {
    // 1. 更新员工部门
    employeeMapper.updateDept(empId, newDeptId, newPosition);
    
    // 2. 更新审批流
    workflowService.updateApprovalNodes(empId, newDeptId);
    
    // 3. 记录调动历史
    transferRecordMapper.insert(buildRecord(empId, oldDeptId, newDeptId, reason));
}
```

**Compensation**: 若审批流更新失败，回滚整个事务；若需要异步处理审批流，使用事件驱动补偿

**Rationale**:
- 声明式事务简化开发
- 审批流更新逻辑可能复杂，建议先同步简化版，后续迭代异步化

**Trade-offs**:
- ✅ 数据一致性有保障
- ✅ 开发成本低
- ❌ 审批流更新阻塞主流程（可优化为异步）

---

### TD-04: 逻辑删除实现
**Decision**: **MyBatis-Plus @TableLogic 注解**  
**Implementation**:
```java
@TableLogic
@TableField(value = "status")
private String status; // ACTIVE, RESIGNED
```

**Query Behavior**:
- `selectList()`: 自动添加 `WHERE status = 'ACTIVE'`
- `updateById()`: 正常更新
- `deleteById()`: 转换为 `UPDATE employee SET status = 'RESIGNED'`

**Rationale**:
- 开箱即用，无需手写 SQL
- 统一的查询行为，避免遗漏状态过滤

**Trade-offs**:
- ✅ 开发效率高
- ✅ 查询自动过滤已删除数据
- ❌ 需要注意关联查询时的状态标识

---

### TD-05: 权限设计
**Decision**: **RBAC + 数据权限拦截器**  
**Implementation**:
- 角色: `SUPER_ADMIN`, `HR`, `DEPT_MANAGER`
- 权限: 全局读写、部门数据隔离
- 数据权限拦截器: 基于 `dept_id` 过滤查询范围

**Example**:
```java
@DataScope(deptAlias = "d")
public List<Employee> listEmployees() {
    return employeeMapper.selectList();
}
```

**Rationale**:
- RBAC 满足角色权限管理
- 数据权限拦截器实现部门级数据隔离

**Trade-offs**:
- ✅ 灵活的权限配置
- ✅ 部门数据隔离透明化
- ❌ 拦截器性能开销（需优化 SQL）

---

## Pending Decisions

| ID | Topic | Status | Deadline |
|----|-------|--------|----------|
| TD-06 | 审批流更新逻辑（硬编码 vs 规则引擎） | Open | Sprint 1 |
| TD-07 | 员工工号生成规则 | Open | Sprint 1 |
| TD-08 | 批量导入员工（Excel）方案 | Open | Sprint 2 |

---

## References

- MyBatis-Plus 逻辑删除: https://baomidou.com/pages/142597/
- 物化路径模式: https://dirtsheet.medium.com/the-materialized-path-pattern-6c9bae8a0f54
- Spring 事务管理: https://spring.io/guides/gs/managing-transactions/