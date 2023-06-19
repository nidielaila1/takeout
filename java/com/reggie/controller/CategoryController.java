package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.common.R;
import com.reggie.entity.Category;
import com.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类信息
     *
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category) {
        log.info("新增分类信息：{}", category.toString());
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize) {
//        配置一个分页构造器
        Page<Category> pageInfo = new Page<Category>(page, pageSize);
//        构造一个条件构造器
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<Category>();
//       在实体类中有一个sort排序，需要进行配置
        lqw.orderByAsc(Category::getSort);
//        进行分页查询
        categoryService.page(pageInfo, lqw);
        return R.success(pageInfo);
    }

    /**
     * 根据id来删除分类
     * 注意：在删除时，需要对当前的分类进行判断，如果当前的分类中关联菜品或者套餐的话，不允许删除
     *
     * @param id
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long id) {
        log.info("删除分类为：{}", id);
        //categoryService.removeById(id);
//        使用自己定义的删除方法
        categoryService.remove(id);
        return R.success("删除成功");
    }

    /**
     * 根据id来修改分类信息
     *
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category) {
        log.info("修改分类信息:{}", category.toString());

        categoryService.updateById(category);
        return R.success("修改分类信息成功");
    }

    /**
     * 根据查询条件来找出分类数据
     *
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category) {
//        构造一个条件构造器
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<>();

//        添加一个条件，根据type来筛选
        lambdaQueryWrapper.eq(category.getType() != null, Category::getType, category.getType());

//        添加一个排序条件
//        1.先通过内置sort属性进行排序         2.再通过更新时间进行排序
        lambdaQueryWrapper.orderByAsc(Category::getSort).orderByAsc(Category::getUpdateTime);


        List<Category> list = categoryService.list(lambdaQueryWrapper);
        return R.success(list);
    }
}
