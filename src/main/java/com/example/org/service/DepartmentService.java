package com.example.org.service;

import com.example.org.entity.Department;
import com.example.org.exception.BusinessException;
import com.example.org.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentService {
    
    private final DepartmentRepository departmentRepository;
    
    /**
     * 获取部门树（懒加载）
     */
    public List<Department> getDepartmentTree() {
        return departmentRepository.findAllActive();
    }
    
    /**
     * 获取指定部门的子部门
     */
    public List<Department> getChildren(Long parentId) {
        if (parentId == null) {
            return departmentRepository.findByParentIdIsNullOrderBySortOrderAsc();
        }
        return departmentRepository.findByParentIdOrderBySortOrderAsc(parentId);
    }
    
    /**
     * 移动部门（拖拽调整层级）
     */
    @Transactional
    public void moveDepartment(Long deptId, Long newParentId) {
        Department dept = departmentRepository.findById(deptId)
            .orElseThrow(() -> new BusinessException("部门不存在: " + deptId));
        
        // 验证不能移动到自己或自己的子部门下
        if (newParentId != null) {
            if (deptId.equals(newParentId)) {
                throw new BusinessException("不能将部门移动到自己");
            }
            
            Department newParent = departmentRepository.findById(newParentId)
                .orElseThrow(() -> new BusinessException("目标部门不存在: " + newParentId));
            
            // 检查目标部门是否是当前部门的子部门
            if (newParent.getPath().startsWith(dept.getPath())) {
                throw new BusinessException("不能将部门移动到自己的子部门下");
            }
            
            // 更新路径
            String oldPath = dept.getPath();
            String newPath = newParent.getPath() + deptId + "/";
            dept.setParentId(newParentId);
            dept.setPath(newPath);
            dept.setLevel(newParent.getLevel() + 1);
            departmentRepository.save(dept);
            
            // 更新所有子部门的路径
            updateChildrenPath(dept.getId(), oldPath, newPath);
        } else {
            // 移动到根节点
            String oldPath = dept.getPath();
            String newPath = "/" + deptId + "/";
            dept.setParentId(null);
            dept.setPath(newPath);
            dept.setLevel(1);
            departmentRepository.save(dept);
            
            updateChildrenPath(dept.getId(), oldPath, newPath);
        }
        
        log.info("部门移动成功: deptId={}, newParentId={}", deptId, newParentId);
    }
    
    /**
     * 递归更新子部门路径
     */
    private void updateChildrenPath(Long parentId, String oldPathPrefix, String newPathPrefix) {
        List<Department> children = departmentRepository.findByParentIdOrderBySortOrderAsc(parentId);
        for (Department child : children) {
            String childOldPath = child.getPath();
            String childNewPath = childOldPath.replace(oldPathPrefix, newPathPrefix);
            child.setPath(childNewPath);
            child.setLevel(child.getLevel() + 1);
            departmentRepository.save(child);
            
            // 递归更新
            updateChildrenPath(child.getId(), childOldPath, childNewPath);
        }
    }
}