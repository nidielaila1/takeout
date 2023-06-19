package com.reggie.entity;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;

//DTO，全称为Data Transfer Object，即数据传输对象，一般用于展示层与服务层之间的数据传输。
//用于封装页面提交的数据
//因为对于有些数据 在封装的时候并不只是含有一个数据库表中的各种数据所以需要新创建一个类进行封装
@Data
//该类直接继承了DIsh并且也有dishflavor中的数据名称
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
