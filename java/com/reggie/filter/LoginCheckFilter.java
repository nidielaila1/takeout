package com.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.reggie.common.BaseContext;
import com.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//用于检查用户是否已经完成了登录  urlPatterns = "/*" 对于所有的请求都进行过滤
@WebFilter(urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    public static AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

//        过滤器具体的处理逻辑如下：
//        1、获取本次请求的URI
        String requestURI = request.getRequestURI();
//        定义不需要处理的请求路径
        String[] urls = {
//                如果用户的行为是登录或者退出，没必要处理
                "/employee/login",
                "/employee/logout",
//                如果用户想看静态资源，不看数据那么随便看
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/login",//移动端登录
                "/user/sendMsg"//移动端发短信
        };

//        2、判断本次请求是否需要处理  (如果当前的请求是在上面的请求中，那么不需要处理)
        boolean check = check(urls, requestURI);

//        3、如果不需要处理，则直接放行
        if (check) {
            log.info("本次请求不需要处理：{}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

//        4-1、判断登录状态，如果已登录，则直接放行
        if (request.getSession().getAttribute("employee") != null) {
            log.info("用户已登录,用户id为：{}", request.getSession().getAttribute("employee"));
//            long id = Thread.currentThread().getId();

//            BaseContext工具类，基于ThreadLocal封装的工具类
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request, response);
            return;
        }


//        4-2、判断移动端用户登录状态，如果已登录，则直接放行
        if (request.getSession().getAttribute("user") != null) {
            log.info("用户已登录,用户id为：{}", request.getSession().getAttribute("user"));

//            BaseContext工具类，基于ThreadLocal封装的工具类
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request, response);
            return;
        }

//        5、如果未登录则返回未登录结果,通过输出流向客户端页面相应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
//        {}表示一个动态的占位符 不需要传统的+号
//        log.info("拦截到请求：{}", request.getRequestURI());
    }


    /*
     * urls[]为上面定义的不需要进行请求判断的参数
     * requestURI为当前请求
     * */
    public boolean check(String[] urls, String requestURI) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match) {
                return true;
            }
        }
        return false;
    }
}
