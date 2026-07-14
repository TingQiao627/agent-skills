# 人员管理系统实施计划

**任务ID**: testDJ-07  
**创建日期**: 2026-07-14  
**目标**: 新增人员管理系统，支持人员基础信息、组织架构归属、状态管理及层级预算信息记录

---

## 1. 需求概述

### 功能范围
1. **人员基础信息管理**：记录人员基本信息及属性
2. **组织架构管理**：支持人员归属组织架构的树形层级结构
3. **状态管理**：定义人员基本状态及状态转换
4. **层级预算信息**：关联层级与预算数据

---

## 2. 数据模型设计

### 2.1 人员基础信息实体 (Person)

| 字段名 | 数据类型 | 必填 | 说明 |
|--------|----------|------|------|
| id | string/UUID | 是 | 人员唯一标识 |
| name | string | 是 | 姓名 |
| employeeNo | string | 是 | 员工编号 |
| organizationId | string/UUID | 是 | 所属组织ID |
| status | enum | 是 | 人员状态 |
| level | int | 否 | 层级（关联预算） |
| createdAt | datetime | 是 | 创建时间 |
| updatedAt | datetime | 是 | 更新时间 |

### 2.2 组织架构实体 (Organization)

| 字段名 | 数据类型 | 必填 | 说明 |
|--------|----------|------|------|
| id | string/UUID | 是 | 组织唯一标识 |
| name | string | 是 | 组织名称 |
| code | string | 是 | 组织编码 |
| parentId | string/UUID | 否 | 父组织ID（树形结构） |
| level | int | 是 | 组织层级 |
| path | string | 否 | 层级路径（便于查询） |
| createdAt | datetime | 是 | 创建时间 |

### 2.3 状态枚举定义

```python
# 人员状态枚举
class PersonStatus:
    ACTIVE = "active"        # 在职
    ON_LEAVE = "on_leave"    # 请假
    RESIGNED = "resigned"    # 离职
    SUSPENDED = "suspended"  # 停职
```

### 2.4 层级预算信息

| 字段名 | 数据类型 | 必填 | 说明 |
|--------|----------|------|------|
| level | int | 是 | 层级编号 |
| levelName | string | 是 | 层级名称 |
| budget | decimal | 是 | 预算金额 |
| currency | string | 否 | 货币单位 |
| validFrom | date | 否 | 生效日期 |
| validTo | date | 否 | 失效日期 |

---

## 3. 文件结构

```
personnel-management/
├── models/
│   ├── person.py          # 人员实体模型
│   ├── organization.py    # 组织架构模型
│   ├── budget.py          # 层级预算模型
│   └── enums.py           # 状态枚举定义
├── services/
│   ├── person_service.py  # 人员管理服务
│   ├── org_service.py     # 组织管理服务
│   └── budget_service.py  # 预算管理服务
├── repositories/
│   ├── person_repo.py     # 人员数据存储
│   ├── org_repo.py        # 组织数据存储
│   └── budget_repo.py     # 预算数据存储
├── data/
│   ├── persons.json       # 人员数据存储
│   ├── organizations.json # 组织数据存储
│   └── budgets.json       # 预算数据存储
└── README.md              # 模块说明文档
```

---

## 4. 全局约束

1. **技术栈**: Python 3.8+ + JSON文件存储（轻量级方案）
2. **数据一致性**: 人员必须归属于有效的组织节点
3. **层级约束**: 组织层级深度建议不超过5级
4. **状态转换**: 仅允许定义的状态枚举值
5. **预算关联**: 层级预算按整数层级值关联，支持时间段生效

---

## 5. 任务分解

### Task 1: 定义数据模型
- 创建 `models/` 目录及实体类
- 定义字段类型验证逻辑
- 编写枚举类

### Task 2: 实现数据存储层
- 创建 `repositories/` 数据访问层
- 实现 JSON 文件读写操作
- 添加基础 CRUD 方法

### Task 3: 实现业务服务层
- 创建 `services/` 业务逻辑层
- 实现人员、组织、预算管理服务
- 添加跨实体关联验证

### Task 4: 编写使用文档
- 编写 README.md 使用说明
- 提供示例数据文件
- 添加 API 使用示例

---

## 6. 验证标准

- [ ] 数据模型字段定义完整且类型正确
- [ ] 组织架构树形结构可正确构建
- [ ] 人员状态枚举覆盖所有业务场景
- [ ] 层级预算关联逻辑清晰
- [ ] 示例数据文件可正常读写