package com.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reggie.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

@Mapper
//加上mapper注解 配置成mapper映射类
public interface EmployeeMapper extends BaseMapper<Employee> {
}
