# Data Model Specification

## Overview
人员管理系统的核心数据模型设计。

## Entity Relationship Diagram

```
Organization (1) ----< (N) Person
     |
     +-- Parent Organization (self-reference)

Person (1) ----< (N) Budget
Organization (1) ----< (N) Budget
```

## Core Entities

### 1. Organization (组织架构)
```java
- id: Long (PK)
- name: String (组织名称)
- code: String (组织编码, unique)
- parentId: Long (上级组织ID, nullable)
- level: Integer (层级)
- path: String (层级路径，如: /总部/技术部/开发组)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

**Constraints**:
- name: NOT NULL, max length 100
- code: NOT NULL, UNIQUE, max length 50
- level: 1-5 (建议最大层级)
- 逻辑删除支持

### 2. Person (人员信息)
```java
- id: Long (PK)
- employeeNo: String (工号, unique)
- name: String (姓名)
- gender: Gender (枚举: MALE/FEMALE/OTHER)
- birthDate: LocalDate (出生日期)
- idCard: String (身份证号, unique)
- phone: String (手机号)
- email: String (邮箱)
- address: String (地址)
- organizationId: Long (所属组织ID, FK)
- position: String (岗位)
- level: String (职级)
- hireDate: LocalDate (入职日期)
- status: PersonStatus (枚举: ACTIVE/INACTIVE/RESIGNED)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

**Constraints**:
- employeeNo: NOT NULL, UNIQUE
- name: NOT NULL, max length 50
- idCard: UNIQUE (18位身份证校验)
- email: Email format
- phone: 11位手机号

### 3. Budget (预算)
```java
- id: Long (PK)
- organizationId: Long (组织ID, FK, nullable)
- personId: Long (人员ID, FK, nullable)
- amount: BigDecimal (预算金额)
- type: BudgetType (枚举: SALARY/BONUS/TRAINING/OTHER)
- period: String (预算周期，如: 2026-Q1)
- startDate: LocalDate (开始日期)
- endDate: LocalDate (结束日期)
- description: String (备注)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

**Constraints**:
- amount: NOT NULL, > 0
- 必须关联 organizationId 或 personId 至少一个
- period: 格式 YYYY-Qn 或 YYYY

## Enums

### Gender
- MALE: 男
- FEMALE: 女
- OTHER: 其他

### PersonStatus
- ACTIVE: 在职
- INACTIVE: 离职
- RESIGNED: 辞职

### BudgetType
- SALARY: 工资预算
- BONUS: 奖金预算
- TRAINING: 培训预算
- OTHER: 其他预算

## Indexes
- Organization: (code), (parentId)
- Person: (employeeNo), (idCard), (organizationId)
- Budget: (organizationId), (personId), (period)

## Data Volume Estimate
- Organization: 100-500 条
- Person: 1,000-10,000 条
- Budget: 10,000-100,000 条