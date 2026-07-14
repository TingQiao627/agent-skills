package com.example.org.service;

import com.example.org.entity.Department;
import com.example.org.entity.Employee;
import com.example.org.entity.TransferRecord;
import com.example.org.exception.BusinessException;
import com.example.org.repository.DepartmentRepository;
import com.example.org.repository.EmployeeRepository;
import com.example.org.repository.TransferRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final TransferRecordRepository transferRecordRepository;
    
    /**
     * 检查字段是否已存在（唯一性校验）
     */
    public boolean checkFieldExists(String field, String value) {
        if ("employeeNo".equals(field)) {
            return employeeRepository.existsByEmployeeNo(value);
        } else if ("phone".equals(field)) {
            return employeeRepository.existsByPhone(value);
        }
        return false;
    }
    
    /**
     * 新增员工
     */
    @Transactional
    public Employee createEmployee(Employee employee, String operator) {
        // 唯一性校验
        if (employeeRepository.existsByEmployeeNo(employee.getEmployeeNo())) {
            throw new BusinessException("工号已存在: " + employee.getEmployeeNo());
        }
        if (employeeRepository.existsByPhone(employee.getPhone())) {
            throw new BusinessException("手机号已存在: " + employee.getPhone());
        }
        
        // 验证部门存在
        Department dept = departmentRepository.findById(employee.getDeptId())
            .orElseThrow(() -> new BusinessException("部门不存在: " + employee.getDeptId()));
        
        employee.setStatus(1);
        employee.setHireDate(LocalDate.now());
        employee.setCreatedBy(operator);
        employee.setUpdatedBy(operator);
        
        Employee saved = employeeRepository.save(employee);
        log.info("员工创建成功: id={}, employeeNo={}, name={}", saved.getId(), saved.getEmployeeNo(), saved.getName());
        
        return saved;
    }
    
    /**
     * 员工调动
     */
    @Transactional
    public void transferEmployee(Long employeeId, Long newDeptId, String newPosition, String reason, String operator) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new BusinessException("员工不存在: " + employeeId));
        
        if (employee.getStatus() == 0) {
            throw new BusinessException("离职员工无法调动");
        }
        
        Department newDept = departmentRepository.findById(newDeptId)
            .orElseThrow(() -> new BusinessException("目标部门不存在: " + newDeptId));
        
        Long oldDeptId = employee.getDeptId();
        String oldPosition = employee.getPosition();
        
        // 创建调动记录
        TransferRecord record = TransferRecord.builder()
            .employeeId(employeeId)
            .employeeName(employee.getName())
            .fromDeptId(oldDeptId)
            .toDeptId(newDeptId)
            .fromPosition(oldPosition)
            .toPosition(newPosition)
            .transferType(newPosition != null && !newPosition.equals(oldPosition) ? 3 : 1)
            .reason(reason)
            .transferDate(LocalDateTime.now())
            .createdBy(operator)
            .build();
        
        transferRecordRepository.save(record);
        
        // 更新员工信息
        employee.setDeptId(newDeptId);
        if (newPosition != null) {
            employee.setPosition(newPosition);
        }
        employee.setUpdatedBy(operator);
        employeeRepository.save(employee);
        
        log.info("员工调动成功: employeeId={}, from={}, to={}", employeeId, oldDeptId, newDeptId);
        
        // TODO: 触发审批流更新（需与审批系统对接）
    }
    
    /**
     * 员工离职（逻辑删除）
     */
    @Transactional
    public void resignEmployee(Long employeeId, LocalDate resignDate, String operator) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new BusinessException("员工不存在: " + employeeId));
        
        if (employee.getStatus() == 0) {
            throw new BusinessException("员工已离职");
        }
        
        employee.setStatus(0);
        employee.setResignDate(resignDate);
        employee.setUpdatedBy(operator);
        employeeRepository.save(employee);
        
        log.info("员工离职成功: employeeId={}, resignDate={}", employeeId, resignDate);
        
        // TODO: 释放系统账号许可（需与权限系统对接）
    }
    
    /**
     * 查询部门员工列表
     */
    public List<Employee> getEmployeesByDept(Long deptId, Integer status) {
        if (status != null) {
            return employeeRepository.findByDeptIdAndStatus(deptId, status);
        }
        return employeeRepository.findByDeptId(deptId);
    }
    
    /**
     * 查询员工详情
     */
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
            .orElseThrow(() -> new BusinessException("员工不存在: " + id));
    }
}