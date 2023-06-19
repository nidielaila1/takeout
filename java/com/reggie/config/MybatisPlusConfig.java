package com.reggie.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


//配置mybatisPlus的分页拦截器
@Configuration
public class MybatisPlusConfig {

    @Bean
//    配置一个mybatisPlus的拦截器 一定要加Bean注解
//    @Bean相当于在配置类中将interceptor添加成这个配置的一个方法，可自动解析
    public MybatisPlusInterceptor interceptor() {
//        创建拦截器
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
//        添加内置分页拦截器
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());

        return interceptor;
    }
}
