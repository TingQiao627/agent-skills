package com.org.arch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.org.arch.entity.Department;
import com.org.arch.entity.Employee;
import com.org.arch.entity.TransferRecord;
import com.org.arch.mapper.DepartmentMapper;
import com.org.arch.mapper.EmployeeMapper;
import com.org.arch.mapper.TransferRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 员工服务
 */
@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeMapper employeeMapper;
    private final DepartmentMapper departmentMapper;
    private final TransferRecordMapper transferRecordMapper;
    
    /**
     * 唯一性校验
     */
    public Map<String, Boolean> checkUniqueness(String field, String value) {
        Long count = 0L;
        if ("employeeNo".equals(field)) {
            count = employeeMapper.selectCount(
                new LambdaQueryWrapper<Employee>().eq(Employee::getEmployeeNo, value)
            );
        } else if ("phone".equals(field)) {
            count = employeeMapper.selectCount(
                new LambdaQueryWrapper<Employee>().eq(Employee::getPhone, value)
            );
        }
        Map<String, Boolean> result = new HashMap<>();
        result.put("isExist", count > 0);
        return result;
    }
    
    /**
     * 新增员工
     */
    @Transactional
    public Employee createEmployee(Employee employee) {
        // 校验部门存在性
        Department dept = departmentMapper.selectById(employee.getDeptId());
        if (dept == null) {
            throw new IllegalArgumentException("所属部门不存在");
        }
        
        // 再次校验工号唯一性
        if (checkUniqueness("employeeNo", employee.getEmployeeNo()).get("isExist")) {
            throw new IllegalArgumentException("工号已存在");
        }
        
        // 再次校验手机号唯一性
        if (checkUniqueness("phone", employee.getPhone()).get("isExist")) {
            throw new IllegalArgumentException("手机号已存在");
        }
        
        // 设置初始状态为在职
        employee.setStatus(1);
        employeeMapper.insert(employee);
        return employee;
    }
    
    /**
     * 分页查询员工列表
     */
    public Page<Employee> getEmployeePage(Long deptId, Integer status, int page, int size) {
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        if (deptId != null) {
            wrapper.eq(Employee::getDeptId, deptId);
        }
        if (status != null) {
            wrapper.eq(Employee::getStatus, status);
        }
        wrapper.orderByDesc(Employee::getCreatedAt);
        return employeeMapper.selectPage(new Page<>(page, size), wrapper);
    }
    
    /**
     * 员工调动
     */
    @Transactional
    public void transferEmployee(Long employeeId, Long newDeptId, String newPosition, String reason) {
        Employee employee = employeeMapper.selectById(employeeId);
        if (employee == null) {
            throw new IllegalArgumentException("员工不存在");
        }
        
        Department newDept = departmentMapper.selectById(newDeptId);
        if (newDept == null) {
            throw new IllegalArgumentException("目标部门不存在");
        }
        
        Department oldDept = departmentMapper.selectById(employee.getDeptId());
        String oldDeptName = oldDept != null ? oldDept.getName() : "";
        
        // 记录调动历史
        TransferRecord record = new TransferRecord();
        record.setEmployeeId(employeeId);
        record.setEmployeeName(employee.getName());
        record.setFromDeptId(employee.getDeptId());
        record.setFromDeptName(oldDeptName);
        record.setToDeptId(newDeptId);
        record.setToDeptName(newDept.getName());
        record.setFromPosition(employee.getPosition());
        record.setToPosition(newPosition);
        record.setReason(reason);
        record.setTransferDate(LocalDateTime.now());
        transferRecordMapper.insert(record);
        
        // 更新员工部门
        employee.setDeptId(newDeptId);
        employee.setPosition(newPosition);
        employeeMapper.updateById(employee);
        
        // TODO: 预留审批流/权限更新接口调用点
    }
    
    /**
     * 员工离职
     */
    @Transactional
    public void resignEmployee(Long employeeId, LocalDate resignDate) {
        Employee employee = employeeMapper.selectById(employeeId);
        if (employee == null) {
            throw new IllegalArgumentException("员工不存在");
        }
        
        employee.setStatus(0);
        employee.setResignDate(resignDate);
        employeeMapper.updateById(employee);
        
        // TODO: 预留系统账号许可释放接口调用点
    }
    
    /**
     * 根据ID查询员工
     */
    public Employee getEmployeeById(Long id) {
        return employeeMapper.selectById(id);
    }
}