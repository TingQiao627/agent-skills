package com.org.arch.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.org.arch.common.Result;
import com.org.arch.entity.Employee;
import com.org.arch.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 员工控制器
 */
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;
    
    /**
     * 唯一性校验
     * GET /api/employees/check?field=employeeNo&value=10086
     */
    @GetMapping("/check")
    public Result<Map<String, Boolean>> checkUniqueness(
            @RequestParam String field,
            @RequestParam String value) {
        return Result.success(employeeService.checkUniqueness(field, value));
    }
    
    /**
     * 新增员工
     * POST /api/employees
     */
    @PostMapping
    public Result<Employee> createEmployee(@RequestBody Employee employee) {
        return Result.success(employeeService.createEmployee(employee));
    }
    
    /**
     * 分页查询员工列表
     * GET /api/employees?deptId=2&status=1&page=1&size=10
     */
    @GetMapping
    public Result<Page<Employee>> getEmployeeList(
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(employeeService.getEmployeePage(deptId, status, page, size));
    }
    
    /**
     * 查询员工详情
     * GET /api/employees/{id}
     */
    @GetMapping("/{id}")
    public Result<Employee> getEmployee(@PathVariable Long id) {
        return Result.success(employeeService.getEmployeeById(id));
    }
    
    /**
     * 员工调动
     * POST /api/employees/{id}/transfer
     */
    @PostMapping("/{id}/transfer")
    public Result<Void> transferEmployee(
            @PathVariable Long id,
            @RequestBody TransferRequest request) {
        employeeService.transferEmployee(id, request.getNewDeptId(), 
            request.getNewPosition(), request.getReason());
        return Result.success();
    }
    
    /**
     * 员工离职
     * PUT /api/employees/{id}/resign
     */
    @PutMapping("/{id}/resign")
    public Result<Void> resignEmployee(
            @PathVariable Long id,
            @RequestBody ResignRequest request) {
        employeeService.resignEmployee(id, request.getResignDate());
        return Result.success();
    }
    
    /**
     * 调动请求DTO
     */
    @lombok.Data
    public static class TransferRequest {
        private Long newDeptId;
        private String newPosition;
        private String reason;
    }
    
    /**
     * 离职请求DTO
     */
    @lombok.Data
    public static class ResignRequest {
        private java.time.LocalDate resignDate;
    }
}