package com.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

//全局的异常捕获器
//对于所有的加了 restController的方法全部进行配置
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    //    只要产生了关于sql语句的异常，那么该方法就会运行
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        log.error(ex.getMessage());
//        对于异常信息进行判断是否有关键字
        if (ex.getMessage().contains("Duplicate entry")) {
//           报错信息 Duplicate entry 'zhangsan' for key 'idx_username'
            String[] split = ex.getMessage().split(" ");
//            根据空格找回重复信息
            String msg = split[2] + "已存在";
//            返回报错信息
            return R.error(msg);
        }
        return R.error("失败了");
    }


    //    只要产生了关于自己定义的业务的异常，那么该方法就会运行
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException customException) {
        log.error(customException.getMessage());

        return R.error(customException.getMessage());
    }
}
