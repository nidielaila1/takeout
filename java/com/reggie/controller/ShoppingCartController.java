package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.reggie.common.BaseContext;
import com.reggie.common.R;
import com.reggie.entity.ShoppingCart;
import com.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加菜品或套餐到购物车中
     *
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        log.info("shoppingCart:{}", shoppingCart.toString());
//        首先给购物车中菜品设置用户id
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

//        查询当前添加的菜品或者套餐是否在购物车中
        Long dishId = shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        if (dishId != null) {
//            当前提交的是菜品
//            根据当前用户id和菜品id来查询是否有重复的
            queryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else {
//            当前提交的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        ShoppingCart one = shoppingCartService.getOne(queryWrapper);

        if (one != null) {
//            如果存在  那么就在原来的基础上数量count+1
            Integer number = one.getNumber();
            one.setNumber(++number);
            shoppingCartService.updateById(one);
        } else {
//            如果没有那么就直接添加，数量默认为1
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            one = shoppingCart;
        }
        return R.success(one);
    }

    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {
        log.info("shoppingCart:{}", shoppingCart.toString());
//        首先给购物车中菜品设置用户id
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

//        查询当前添加的菜品或者套餐是否在购物车中
        Long dishId = shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        if (dishId != null) {
//            当前提交的是菜品
//            根据当前用户id和菜品id来查询是否有重复的
            queryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else {
//            当前提交的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        ShoppingCart one = shoppingCartService.getOne(queryWrapper);
        Integer num = one.getNumber();

        if (num == 1) {
//            如果当前菜品或套餐的数量只有一个，那么就直接删除
            shoppingCartService.removeById(one);
            return R.success(null);
        } else {
//            如果没有那么就直接数量减1
            one.setNumber(--num);
            shoppingCartService.save(one);
        }
        shoppingCart = one;
        return R.success(shoppingCart);
    }

    /**
     * 查看购物车
     *
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        log.info("查看购物车");
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();

//        查当前用户的所有添加成功菜品
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());

//        根据创建时间升序来排
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return R.success(list);
    }

    /**
     * 清除购物车中所有数据
     *
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean() {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);
        return R.success("清空购物车成功");
    }
}
