package com.org.arch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.org.arch.entity.Department;
import org.apache.ibatis.annotations.Mapper;

/**
 * 部门 Mapper 接口
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
}