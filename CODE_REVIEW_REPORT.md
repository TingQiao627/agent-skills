# 代码评审报告 - 组织架构与人员管理系统

> **评审日期**: 2026-07-14  
> **评审范围**: 后端核心实现 (Spring Boot + MyBatis Plus)  
> **评审标准**: Java 17 + Spring Boot 3.x 最佳实践

---

## 一、高层架构评审

### ✅ 优点
1. **分层清晰**: Controller → Service → Mapper 三层架构明确
2. **技术选型合理**: Spring Boot 3.2.0 + MyBatis Plus 3.5.5 + Java 17 符合主流
3. **统一响应封装**: `Result<T>` 统一响应格式，符合需求文档 API 规范

### ⚠️ 架构问题

| 问题 | 影响 | 建议 |
|------|------|------|
| **缺少全局异常处理器** | 异常时返回非标准格式，前端无法统一处理 | 添加 `@ControllerAdvice` 全局异常处理类 |
| **缺少 DTO 层** | Controller 内部类作为 DTO，不符合工程规范 | 将 `MoveRequest`、`TransferRequest` 等抽取为独立 DTO 类 |
| **缺少事务控制** | Service 层关键方法未加 `@Transactional` | 在调动、批量更新等方法上添加事务注解 |

---

## 二、安全性评审 (🔴 高优先级)

### 1. 敏感信息泄露
**文件**: `src/main/resources/application.yml:8`

```yaml
password: root  # ❌ 硬编码数据库密码
```

**风险**: 生产环境泄露数据库凭据  
**修复方案**: 使用环境变量或配置中心
```yaml
password: ${DB_PASSWORD:root}
```

---

### 2. 参数校验缺失
**文件**: `EmployeeController.java`、`DepartmentController.java`

**问题**: 虽然引入了 `spring-boot-starter-validation`，但 Controller 层未使用校验注解  
**示例**:
```java
// 当前实现
@PostMapping
public Result<Employee> add(@RequestBody Employee employee) {
    return Result.success(employeeService.addEmployee(employee));
}

// 应改为
@PostMapping
public Result<Employee> add(@Valid @RequestBody Employee employee) {
    return Result.success(employeeService.addEmployee(employee));
}
```

**建议**: 在实体类或 DTO 上添加校验注解
```java
@Data
public class Employee {
    @NotBlank(message = "姓名不能为空")
    private String name;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    @NotBlank(message = "工号不能为空")
    private String employeeNo;
}
```

---

### 3. SQL 注入防护
**评估**: ✅ 使用 MyBatis Plus，所有查询均通过参数绑定，无 SQL 注入风险

---

## 三、业务逻辑评审 (🔴 高优先级)

### 1. 部门移动循环引用风险
**文件**: `DepartmentService.java:moveDepartment`

**问题**: 将部门移动到自己或自己的子部门下会导致循环引用  
**修复方案**: 在移动前检查目标父节点是否为当前节点或其子节点

```java
public void moveDepartment(Long id, Long newParentId) {
    Department dept = getById(id);
    if (dept == null) {
        throw new RuntimeException("部门不存在");
    }
    
    // ❌ 缺少循环引用检查
    // 修复: 检查 newParentId 是否为当前节点或其子节点
    if (newParentId.equals(id)) {
        throw new RuntimeException("不能将部门移动到自己");
    }
    
    if (isChild(newParentId, id)) {
        throw new RuntimeException("不能将部门移动到自己的子部门");
    }
    
    // 原有逻辑...
}

private boolean isChild(Long parentId, Long childId) {
    // 查询父节点的所有子节点，判断是否包含 childId
    List<Department> children = departmentMapper.selectList(
        new LambdaQueryWrapper<Department>()
            .eq(Department::getParentId, parentId)
    );
    for (Department child : children) {
        if (child.getId().equals(childId)) {
            return true;
        }
        if (isChild(child.getId(), childId)) {
            return true;
        }
    }
    return false;
}
```

---

### 2. 员工调动事务一致性
**文件**: `EmployeeService.java:transfer`

**问题**: 调动操作涉及多表更新（员工表、调动记录表），但缺少事务控制  
**风险**: 部分成功时数据不一致  
**修复方案**:
```java
@Transactional(rollbackFor = Exception.class)  // 添加事务注解
public void transfer(Long id, Long newDeptId, String newPosition, String reason) {
    // 原有逻辑...
}
```

---

### 3. 逻辑删除配置冲突
**文件**: `application.yml:18-20`

```yaml
logic-delete-field: status
logic-delete-value: 0      # ❌ 通常删除是 1
logic-not-delete-value: 1  # ❌ 通常未删除是 1
```

**问题**: 
- 需求要求员工离职"逻辑删除"，但配置与常规理解相反
- `Employee` 实体中 `status` 字段使用枚举 `EmployeeStatus`，可能与 MyBatis Plus 逻辑删除机制冲突

**建议**: 明确 `status` 字段的语义：
- 如果使用枚举（IN_SERVICE/RESIGNED），则不应配置 MyBatis Plus 逻辑删除
- 如果使用 0/1 标志，则需统一字段含义

---

### 4. 调动记录冗余字段一致性
**文件**: `TransferRecord.java`

```java
private String employeeName;  // 冗余字段
private String fromDeptName;  // 冗余字段
private String toDeptName;    // 冗余字段
```

**评估**: ✅ 符合需求"记录留痕"要求，保留历史快照  
**注意**: 员工改名或部门更名后，历史记录不受影响（符合业务需求）

---

## 四、代码质量评审

### 1. Result 类错误码设计
**文件**: `Result.java:26`

```java
public static <T> Result<T> error(String msg) {
    return new Result<>(500, msg, null);  // ❌ 所有错误都返回 500
}
```

**问题**: 不符合 RESTful 最佳实践  
**修复建议**:
```java
public static <T> Result<T> badRequest(String msg) {
    return new Result<>(400, msg, null);
}

public static <T> Result<T> notFound(String msg) {
    return new Result<>(404, msg, null);
}

public static <T> Result<T> conflict(String msg) {
    return new Result<>(409, msg, null);
}
```

---

### 2. 部门树递归性能问题
**文件**: `DepartmentService.java:buildTree`

**问题**: 每次查询部门树都递归遍历所有节点，大数据量时性能堪忧  
**建议**: 
- 增加 Redis 缓存部门树
- 或使用闭包表（Closure Table）存储层级关系

---

### 3. Service 层缺少事务边界
**文件**: `DepartmentService.java`、`EmployeeService.java`

**问题**: 所有写操作方法均未标注 `@Transactional`  
**风险**: 复杂业务操作可能数据不一致  
**修复**: 在 Service 类上添加 `@Transactional(readOnly = true)`，在写方法上覆盖为 `@Transactional`

---

## 五、缺失功能清单

| 功能 | 文件 | 状态 |
|------|------|------|
| 全局异常处理器 | 缺失 | 🔴 必须补充 |
| 参数校验实现 | Controller 层缺失 | 🔴 必须补充 |
| API 文档 (Swagger) | 缺失 | 🟡 建议补充 |
| 单元测试 | 缺失 | 🟡 建议补充 |
| 日志框架集成 | 仅有 StdOutImpl | 🟡 建议使用 Logback |
| 数据库连接池监控 | 缺失 | 🟢 可选 |

---

## 六、评审结论

### ✅ 通过项
- 基础架构设计合理
- MyBatis Plus 集成正确
- 核心 CRUD 实现完整
- 符合需求文档 API 规范

### 🔴 必须修复项（阻塞性）
1. **安全**: 数据库密码硬编码 → 使用环境变量
2. **业务**: 部门移动循环引用检查 → 添加前置校验
3. **事务**: 员工调动缺少事务控制 → 添加 `@Transactional`
4. **健壮性**: 缺少全局异常处理器 → 添加 `@ControllerAdvice`

### 🟡 建议优化项（非阻塞）
1. 参数校验注解完善
2. DTO 类抽取独立文件
3. Result 错误码细化
4. 部门树缓存机制

---

## 七、下一步行动

优先级从高到低：

1. **修复安全问题**: 移除硬编码密码，使用环境变量
2. **添加全局异常处理器**: `GlobalExceptionHandler.java`
3. **补全事务控制**: 在 Service 写方法上添加 `@Transactional`
4. **修复循环引用检查**: `DepartmentService.moveDepartment` 前置校验
5. **补充参数校验**: 实体类添加 Validation 注解
6. **抽取 DTO 类**: Controller 内部类独立为 DTO 包

---

**评审人**: AI Code Review Agent  
**评审状态**: ⚠️ 有条件通过（需修复阻塞项）