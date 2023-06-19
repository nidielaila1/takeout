package com.reggie.common;

/**
 * 基于ThreadLocal封装的工具类，用于保存和获取当前用户的id
 * 实现步骤：
 * 1、编写BaseContext工具类，基于ThreadLocal封装的工具类
 * 2、在LoginCheckFilter的doFilter方法中调用BaseContext来设置当前登录用户的id
 * 3、在MyMetaObjectHandler的方法中调用BaseContext获取登录用户的id
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 用来获取值
     *
     * @return
     */
    public static Long getCurrentId() {
        return threadLocal.get();
    }

    /**
     * 用来设置值
     *
     * @param id
     */
    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }
}
