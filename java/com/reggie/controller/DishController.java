package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.common.R;
import com.reggie.entity.Category;
import com.reggie.entity.Dish;
import com.reggie.entity.DishDto;
import com.reggie.entity.DishFlavor;
import com.reggie.service.CategoryService;
import com.reggie.service.DishFlavorService;
import com.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 保存菜品信息
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info("DishDto:{}", dishDto);
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }


    /**
     * 菜品信息分类查询
     * <p>
     * 页面中需要一个菜品的分类信息，但是在仅仅通过dish类来进行返回不能完成
     * 需要一个dishdto类（其中包括菜品的分类信息）
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPageInfo = new Page<>(page, pageSize);
//        需要将查询到的dish的pageInfo中的所有数据拷贝到dishDtoPageInfo中

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        添加一个模糊查询的过滤条件
        queryWrapper.like(!StringUtils.isEmpty(name), Dish::getName, name);
//        添加一个排序条件，根据更新时间来排序
        queryWrapper.orderByDesc(Dish::getUpdateTime);

//        执行分页查询
        dishService.page(pageInfo, queryWrapper);

//        进行对象的拷贝
//        第一个参数，被拷贝的数据
//        第二个参数，拷贝到哪
//        第三个参数，需要进行忽略的参数
        BeanUtils.copyProperties(pageInfo, dishDtoPageInfo, "records");

//        由于dishDtoPageInfo中的records被忽略了，所以需要自己进行配置
        List<Dish> dishRecords = pageInfo.getRecords();

//        通过流的方式对于list集合中的每个对象进行配置
        List<DishDto> list = dishRecords.stream().map((item) -> {

            DishDto dishDto = new DishDto();
//            需要通过对象的拷贝，将item中的所有数据拷贝到这个dishdto中
            BeanUtils.copyProperties(item, dishDto);

//            通过菜品类别id找出该菜品类别
//            并给dishdto填入该种类的值
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());

//        由于上面忽略了recordsList集合，所以我在下面对于他进行封装
        dishDtoPageInfo.setRecords(list);

        return R.success(dishDtoPageInfo);
    }

    /**
     * 因为id在请求的url中 使用@PathVariable
     * 因为要根据id查出对应的菜品信息与对应的菜品口味信息
     * 并将上面两个封装到dishdto中返回给前端页面
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 更新菜品
     *
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {

        dishService.updateWithFlavor(dishDto);

//        清理某个分类下面的菜品缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return R.success("修改菜品成功");
    }


    /**
     * 根据条件查询对应的菜品信息
     *
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        List<DishDto> dishDtoList = null;

//        构造一个key，动态的设计一个key
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();

//        先从redis中获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        if (dishDtoList != null) {
            //如果存在那么就直接返回，不需要查询数据库
            return R.success(dishDtoList);
        }
//        如果不存那么需要查询数据库，将查询到的菜品放到Redis中

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        根据菜品分类id查询
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
//        根据在前端的输入菜品名进行模糊查询
        queryWrapper.like(dish.getName() != null, Dish::getName, dish.getName());
//        添加一个条件，只查询状态为起售的  0 停售 1 起售
        queryWrapper.eq(Dish::getStatus, 1);


//        添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

//        查出菜品信息
        List<Dish> list = dishService.list(queryWrapper);

        dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
//            需要通过对象的拷贝，将item中的所有数据拷贝到这个dishdto中
            BeanUtils.copyProperties(item, dishDto);

//            通过菜品类别id找出该菜品类别
//            并给dishdto填入该种类的值
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

//            当前菜品的id
            Long dishId = item.getId();

//            根据菜品id查出当前菜品对应口味集合
            LambdaQueryWrapper<DishFlavor> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(DishFlavor::getDishId, dishId);

            List<DishFlavor> dishFlavorList = dishFlavorService.list(queryWrapper1);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());


//        设置redis中的缓存，设置60分钟的存活时间
        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);


        return R.success(dishDtoList);
    }

}
