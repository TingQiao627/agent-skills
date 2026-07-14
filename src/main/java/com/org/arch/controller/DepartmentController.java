package com.org.arch.controller;

import com.org.arch.common.Result;
import com.org.arch.entity.Department;
import com.org.arch.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门控制器
 */
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentService departmentService;
    
    /**
     * 获取部门树
     * GET /api/departments/tree
     */
    @GetMapping("/tree")
    public Result<List<Department>> getDepartmentTree() {
        return Result.success(departmentService.getDepartmentTree());
    }
    
    /**
     * 获取子部门（懒加载）
     * GET /api/departments/{id}/children
     */
    @GetMapping("/{id}/children")
    public Result<List<Department>> getChildren(@PathVariable Long id) {
        return Result.success(departmentService.getChildren(id));
    }
    
    /**
     * 移动部门
     * PUT /api/departments/{id}/move
     */
    @PutMapping("/{id}/move")
    public Result<Void> moveDepartment(@PathVariable Long id, @RequestBody MoveRequest request) {
        departmentService.moveDepartment(id, request.getNewParentId());
        return Result.success();
    }
    
    /**
     * 移动请求DTO
     */
    @lombok.Data
    public static class MoveRequest {
        private Long newParentId;
    }
}