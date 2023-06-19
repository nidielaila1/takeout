package com.reggie.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String name;

    private String password;

    private String phone;

    private String sex;

    //与数据库中表中不一致，需要在yml配置文件中加入    map-underscore-to-camel-case: true 字段配置成驼峰命名法
    private String idNumber;

    //用于判断 当前员工是否被锁定
    private Integer status;

    @TableField(fill = FieldFill.INSERT)//只有在创建的时候会被自动填充
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)//创建和修改时自动填充
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)//只有在创建的时候会被自动填充
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)//创建和修改时自动填充
    private Long updateUser;

}
