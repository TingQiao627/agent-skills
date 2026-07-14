package com.org.arch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.org.arch.entity.Department;
import com.org.arch.mapper.DepartmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门服务
 */
@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentMapper departmentMapper;
    
    /**
     * 获取完整部门树
     */
    public List<Department> getDepartmentTree() {
        List<Department> allDepartments = departmentMapper.selectList(
            new LambdaQueryWrapper<Department>().orderByAsc(Department::getSortOrder)
        );
        return buildTree(allDepartments, 0L);
    }
    
    /**
     * 获取指定部门的子部门（懒加载）
     */
    public List<Department> getChildren(Long parentId) {
        return departmentMapper.selectList(
            new LambdaQueryWrapper<Department>()
                .eq(Department::getParentId, parentId)
                .orderByAsc(Department::getSortOrder)
        );
    }
    
    /**
     * 移动部门到新父节点
     */
    @Transactional
    public void moveDepartment(Long id, Long newParentId) {
        Department dept = departmentMapper.selectById(id);
        if (dept == null) {
            throw new IllegalArgumentException("部门不存在");
        }
        
        // 验证：不能移动到自己或自己的子节点下
        if (id.equals(newParentId) || isDescendant(id, newParentId)) {
            throw new IllegalArgumentException("不能移动到自己或子部门下");
        }
        
        // 更新父节点
        dept.setParentId(newParentId);
        
        // 重新计算层级和路径
        if (newParentId == 0L) {
            dept.setLevel(1);
            dept.setPath(String.valueOf(id));
        } else {
            Department parent = departmentMapper.selectById(newParentId);
            if (parent == null) {
                throw new IllegalArgumentException("目标父部门不存在");
            }
            dept.setLevel(parent.getLevel() + 1);
            dept.setPath(parent.getPath() + "/" + id);
        }
        
        departmentMapper.updateById(dept);
        
        // 递归更新子部门的层级和路径
        updateChildrenPath(id, dept.getPath(), dept.getLevel());
    }
    
    private List<Department> buildTree(List<Department> all, Long parentId) {
        return all.stream()
            .filter(d -> parentId.equals(d.getParentId()))
            .peek(d -> d.setChildren(buildTree(all, d.getId())))
            .collect(Collectors.toList());
    }
    
    private boolean isDescendant(Long ancestorId, Long descendantId) {
        List<Department> children = getChildren(ancestorId);
        for (Department child : children) {
            if (child.getId().equals(descendantId) || isDescendant(child.getId(), descendantId)) {
                return true;
            }
        }
        return false;
    }
    
    private void updateChildrenPath(Long parentId, String parentPath, int parentLevel) {
        List<Department> children = getChildren(parentId);
        for (Department child : children) {
            child.setLevel(parentLevel + 1);
            child.setPath(parentPath + "/" + child.getId());
            departmentMapper.updateById(child);
            updateChildrenPath(child.getId(), child.getPath(), child.getLevel());
        }
    }
}