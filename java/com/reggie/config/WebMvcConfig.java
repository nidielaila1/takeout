package com.reggie.config;

import com.reggie.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;


@Slf4j
//用来配置静态资源的配置类
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {

    //    配置静态资源映射
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开启静态资源映射................");
//        添加前端处理器 （也就是mvc的静态资源解析器）
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
    }

    /**
     * 扩展mvc的消息转换器
     *
     * @param converters
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("开启消息转换器");
        //创建消息转换器对象
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
//        设置对象转换器 底层使用Jackson将java对象转换成json
        messageConverter.setObjectMapper(new JacksonObjectMapper());
//        修改转换器类中的优先级顺序，将自己创建的设置成第一个
        converters.add(0, messageConverter);
    }
}
