# OPSX Proposal: 组织架构与人员管理系统

## Metadata
- **Proposal ID**: testdj-04-organization-management
- **Title**: 组织架构与人员管理系统
- **Status**: draft
- **Created**: 2026-07-14
- **Author**: AI Agent

---

## Problem Statement

随着团队规模扩大，人员与部门关系变得复杂，现有系统缺乏：
- 部门树形结构的可视化与灵活调整能力
- 员工全生命周期管理（入职、调动、离职）
- 与其他业务系统（审批、权限）的数据联动
- 唯一性校验与防重机制
- 离职员工的逻辑删除与数据隔离

---

## Goals

### Primary Goals
1. 实现部门树形结构的懒加载展示与拖拽调整
2. 实现员工新增的唯一性实时校验
3. 实现员工跨部门调动，级联更新审批流并留痕
4. 实现员工离职的逻辑删除与权限释放

### Non-Goals
- 薪酬管理模块
- 考勤打卡功能
- 招聘流程管理

---

## Detailed Requirements

### R1: 部门树形结构的加载与交互

#### R1.1 功能需求
- 左侧展示部门树，默认展开第一级
- 点击节点加载该部门下的人员列表
- 懒加载：点击展开按钮时才请求子部门
- 拖拽调整部门层级（如将"前端组"拖拽到"研发二部"下）

#### R1.2 API Specification
```
GET /api/departments/tree
Response:
{
  "code": 200,
  "data": [{
    "id": 1,
    "name": "研发部",
    "parentId": null,
    "children": [{
      "id": 2,
      "name": "前端组",
      "parentId": 1,
      "children": []
    }]
  }]
}

PUT /api/departments/{id}/move
Request:
{
  "newParentId": 5
}
Response:
{
  "code": 200,
  "msg": "部门调整成功"
}
```

#### R1.3 数据模型
```sql
CREATE TABLE department (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  parent_id BIGINT NULL,
  path VARCHAR(500), -- 物化路径，如 "1.2.5"
  level INT DEFAULT 1,
  sort_order INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_parent_id (parent_id),
  INDEX idx_path (path)
);
```

#### R1.4 业务规则
- 不能将部门拖拽到自己的子部门下（防止循环引用）
- 拖拽后需更新所有子部门的 `path` 和 `level`
- 部门名称在同级唯一
- **部门下有人员时禁止删除**（需先清空或转移人员）

#### R1.5 部门删除接口
```
DELETE /api/departments/{id}
Response (成功):
{
  "code": 200,
  "msg": "部门删除成功"
}
Response (失败 - 部门下有人员):
{
  "code": 40005,
  "msg": "该部门下仍有员工，无法删除"
}
```

---

### R2: 员工新增（唯一性校验与防重）

#### R2.1 功能需求
- 右侧表单录入员工信息（姓名、工号、手机号、所属部门、职位）
- 实时校验：输入工号/手机号后，光标移开时实时检查是否重复
- 重复时输入框标红提示

#### R2.2 API Specification
```
GET /api/employees/check?field=employeeNo&value=10086
Response:
{
  "code": 200,
  "data": {
    "isExist": false
  }
}

POST /api/employees
Request:
{
  "name": "张三",
  "employeeNo": "10086",
  "deptId": 2,
  "phone": "13800138000",
  "position": "前端开发工程师"
}
Response:
{
  "code": 200,
  "data": {
    "id": 123
  },
  "msg": "员工新增成功"
}
```

#### R2.3 数据模型
```sql
CREATE TABLE employee (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  employee_no VARCHAR(20) NOT NULL UNIQUE,
  phone VARCHAR(20) NOT NULL UNIQUE,
  dept_id BIGINT NOT NULL,
  position VARCHAR(100),
  status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, RESIGNED
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_dept_id (dept_id),
  INDEX idx_status (status)
);
```

#### R2.4 业务规则
- 工号全局唯一（数据库唯一索引）
- 手机号全局唯一（数据库唯一索引）
- 所属部门ID必须存在
- 新增时默认状态为 `ACTIVE`

---

### R3: 人员调动（级联更新与快照）

#### R3.1 功能需求
- 在员工详情页点击"调动"，选择目标部门和新职位
- 弹出警告提示："调动后，该员工相关的审批流/权限将发生变化，确认调动？"
- 调动后级联更新审批流节点
- 记录调动历史留痕

#### R3.2 API Specification
```
POST /api/employees/{id}/transfer
Request:
{
  "newDeptId": 3,
  "newPosition": "Java开发",
  "reason": "业务调整"
}
Response:
{
  "code": 200,
  "msg": "调动成功"
}
```

#### R3.3 数据模型
```sql
CREATE TABLE transfer_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  employee_id BIGINT NOT NULL,
  from_dept_id BIGINT NOT NULL,
  to_dept_id BIGINT NOT NULL,
  from_position VARCHAR(100),
  to_position VARCHAR(100),
  reason VARCHAR(500),
  transferred_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_employee_id (employee_id)
);
```

#### R3.4 业务规则
- 更新员工的 `dept_id`
- 触发更新该员工相关的默认审批流节点
- 写入调动记录表留痕
- 目标部门必须存在

---

### R4: 员工离职（逻辑删除与状态隔离）

#### R4.1 功能需求
- 点击"办理离职"，选择离职日期
- 列表页支持筛选状态（在职/离职）
- 离职人员显示灰色标签，不可编辑

#### R4.2 API Specification
```
PUT /api/employees/{id}/resign
Request:
{
  "resignDate": "2023-11-01"
}
Response:
{
  "code": 200,
  "msg": "离职办理成功"
}

GET /api/employees?status=ACTIVE
GET /api/employees?status=RESIGNED
```

#### R4.3 数据模型（扩展 R2.3）
```sql
ALTER TABLE employee ADD COLUMN resign_date DATE;
ALTER TABLE employee ADD COLUMN resigned_at TIMESTAMP;
```

#### R4.4 业务规则
- **严禁物理删除**：将员工状态更新为 `RESIGNED`
- 自动释放该员工占用的系统账号许可
- 清除系统登录权限
- 历史考勤、审批数据保留，查询时带状态标识

---

## Implementation Notes

### 技术栈建议
- **后端**: Spring Boot 3.x + MyBatis-Plus
- **数据库**: MySQL 8.0+
- **前端**: React 18 + Ant Design Tree组件

### 关键实现点
1. **部门树懒加载**: 使用递归查询或物化路径模式
2. **拖拽防循环**: 后端校验目标节点是否为当前节点的子孙
3. **唯一性校验**: 前端防抖 + 后端数据库唯一索引双重保障
4. **调动事务**: 使用 `@Transactional` 确保 employee 更新、审批流更新、记录插入三者原子性
5. **逻辑删除**: MyBatis-Plus `@TableLogic` 注解自动处理

### 异常兜底策略
详见 `testdj-04-organization-management.exception-handling.md`，核心要点：
- **全局异常处理**: 分层设计（BusinessException / SystemException）
- **核心场景兜底**: 拖拽循环引用、唯一性冲突、调动失败、离职权限释放
- **补偿机制**: 补偿任务表 + 定时补偿任务（审批流更新、权限释放）
- **前端提示**: 统一错误码映射 + axios 拦截器统一处理
- **监控告警**: 补偿任务堆积、事务回滚率、外部服务失败率

### 权限设计
- **超管/HR**: 全局读写权限
- **部门主管**: 通过数据权限拦截器，仅查询本部门及下属部门，仅编辑本部门人员部分字段

---

## Risks and Mitigation

| Risk | Impact | Mitigation |
|------|--------|------------|
| 拖拽导致循环引用 | 系统崩溃、数据混乱 | 后端强校验 + 前端预检查 + 循环引用异常拦截 |
| 高并发工号重复 | 数据一致性受损 | 数据库唯一索引兜底 + 后端二次校验 + 分布式锁（可选） |
| 调动事务失败 | 数据不一致 | Spring事务管理 + 补偿任务机制 + 监控告警 |
| 离职误操作 | 业务中断 | 离职前二次确认 + 管理员恢复接口 |
| 审批流更新失败 | 数据不一致 | 降级处理 + 补偿任务 + 定时重试 |
| 权限释放失败 | 安全风险 | 补偿任务 + 定时重试 + 失败告警 |
| 误删有员工部门 | 数据丢失 | 删除前员工/子部门校验 + 异常拦截 + 前端确认提示 |

---

## Open Questions

1. 部门树的层级深度是否有上限？（建议 ≤ 5级）
2. 员工工号格式是否有规则？（纯数字/字母数字混合）
3. 调动后的审批流更新逻辑是否需要配置化？（硬编码 vs 规则引擎）
4. 离职后是否需要保留登录历史查询功能？
5. 是否需要支持批量导入员工（Excel）？

---

## Success Criteria

1. 部门树加载时间 < 1s（1000节点）
2. 唯一性校验响应时间 < 200ms
3. 调动操作成功率 > 99.9%
4. 离职员工数据隔离验证通过
5. 前端拖拽操作流畅无卡顿

---

## References

- 类似系统: 钉钉组织架构、飞书通讯录
- 数据库设计: 闭包表 vs 物化路径 vs 嵌套集
- 前端组件: Ant Design Tree Drag