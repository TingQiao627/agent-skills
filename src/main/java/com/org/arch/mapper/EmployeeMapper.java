package com.org.arch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.org.arch.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工 Mapper 接口
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}