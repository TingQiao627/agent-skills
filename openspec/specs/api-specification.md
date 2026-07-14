# API Specification

## Overview
人员管理系统 RESTful API 设计规范。

## Base URL
```
http://localhost:8080/api/v1
```

## Common Response Format

### Success Response
```json
{
  "code": 200,
  "message": "Success",
  "data": { ... }
}
```

### Error Response
```json
{
  "code": 400,
  "message": "Validation failed",
  "errors": [
    {"field": "name", "message": "姓名不能为空"}
  ]
}
```

## API Endpoints

### Organization APIs

#### 1. 创建组织
```
POST /organizations
Request:
{
  "name": "技术部",
  "code": "TECH",
  "parentId": 1
}
Response: 201 Created
{
  "id": 2,
  "name": "技术部",
  "code": "TECH",
  "parentId": 1,
  "level": 2,
  "path": "/总部/技术部"
}
```

#### 2. 获取组织树
```
GET /organizations/tree
Response:
[
  {
    "id": 1,
    "name": "总部",
    "code": "ROOT",
    "children": [...]
  }
]
```

#### 3. 更新组织
```
PUT /organizations/{id}
```

#### 4. 删除组织
```
DELETE /organizations/{id}
```

### Person APIs

#### 5. 创建人员
```
POST /persons
Request:
{
  "employeeNo": "EMP001",
  "name": "张三",
  "gender": "MALE",
  "birthDate": "1990-01-15",
  "idCard": "110101199001150123",
  "phone": "13800138000",
  "email": "zhangsan@example.com",
  "organizationId": 2,
  "position": "高级工程师",
  "level": "P7",
  "hireDate": "2020-03-01"
}
Response: 201 Created
```

#### 6. 查询人员列表
```
GET /persons?page=0&size=20&organizationId=2&status=ACTIVE
Response:
{
  "content": [...],
  "totalElements": 100,
  "totalPages": 5,
  "number": 0
}
```

#### 7. 获取人员详情
```
GET /persons/{id}
```

#### 8. 更新人员
```
PUT /persons/{id}
```

#### 9. 删除人员
```
DELETE /persons/{id}
```

### Budget APIs

#### 10. 创建预算
```
POST /budgets
Request:
{
  "organizationId": 2,
  "amount": 500000.00,
  "type": "SALARY",
  "period": "2026-Q1",
  "startDate": "2026-01-01",
  "endDate": "2026-03-31"
}
```

#### 11. 查询预算列表
```
GET /budgets?organizationId=2&period=2026-Q1
```

#### 12. 更新预算
```
PUT /budgets/{id}
```

#### 13. 删除预算
```
DELETE /budgets/{id}
```

### Import/Export APIs

#### 14. 导出人员数据
```
GET /persons/export?format=excel&organizationId=2
Response: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
```

#### 15. 导入人员数据
```
POST /persons/import
Content-Type: multipart/form-data
Request: file (Excel/CSV)
Response:
{
  "totalRows": 100,
  "successCount": 98,
  "failCount": 2,
  "errors": [
    {"row": 5, "message": "工号已存在"},
    {"row": 23, "message": "身份证号格式错误"}
  ]
}
```

#### 16. 下载导入模板
```
GET /persons/import/template
Response: Excel template file
```

## HTTP Status Codes
- 200: OK
- 201: Created
- 204: No Content
- 400: Bad Request
- 404: Not Found
- 409: Conflict
- 500: Internal Server Error

## Pagination
使用 Spring Data 标准分页：
- `page`: 页码（从0开始）
- `size`: 每页大小
- `sort`: 排序字段（如: name,asc）