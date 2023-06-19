package com.reggie.common;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/*
 *
 * 通用返回结果，服务端访问的数据都会封装成为该结果
 *
 * */

@Data
public class R<T> implements Serializable {

    private Integer code; //编码：1成功，0和其它数字为失败

    private String msg; //错误信息

    //    使用泛型，只需要将返回数据封装成一个实体类即可
    private T data; //数据

    private Map map = new HashMap(); //动态数据

    //    如果成功 需要传入 相关的给到前端的封装的对象
    public static <T> R<T> success(T object) {
        R<T> r = new R<T>();
        r.data = object;
        r.code = 1;
        return r;
    }

    //    如果失败，只需要放入一个字符串即可
    public static <T> R<T> error(String msg) {
        R r = new R();
        r.msg = msg;
        r.code = 0;
        return r;
    }

    public R<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

}
