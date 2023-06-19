package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.common.R;
import com.reggie.entity.Employee;
import com.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/employee")
//一定要加上RestController注解 配置成Controller注解类
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;


    @PostMapping("/login")
//    因为在前端中使用的为一个post请求，所以直接使用postMapping注解
//    使用RequestBody直接将数据封装成一个Employee类型的数据
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
//        写程序前首先将逻辑写好，在进行编程
//        处理逻辑如下：
//        1、将页面提交的密码password进行md5加密处理
//        2、根据页面提交的用户名username查询数据库
//        3、如果没有查询到则返回登录失败结果
//        4、密码比对，如果不一致则返回登录失败结果
//        5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
//        6、登录成功，将员工id存入Session并返回登录成功结果

//        1.首先对用户密码进行md5加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

//        2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<>();
//        对于username字段设置了一个唯一约束
//        可以使用getOne方法获取相应数据
        lqw.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(lqw);
//        3.判断是否查到
        if (emp == null) {
            return R.error("登录失败");
        }

//      4.如果查到了，应该对于数据库中的代码进行比对
        if (!emp.getPassword().equals(password)) {
            return R.error("登陆失败");
        }

//      5.查看员工状态，如果为禁用状态返回员工已禁用结果
//        state为1为启用状态 0表示为禁用
        if (emp.getStatus() == 0) {
            return R.error("账号已禁用");
        }
//        6、登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee", emp.getId());
//        登录成功使用R.success将emp实体类存入返回
        return R.success(emp);
    }

    @PostMapping("/logout")
    public R<String> Logout(HttpServletRequest request) {
//        退出并清理session中保存的用户id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
//        先进行测试
        log.info("员工信息：{}", employee.toString());

//        设置一个初始密码，需要进行md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

//        设置成当前系统的时间
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());

//        使用request获取当前登录用户的id 并将创建当前用户的信息设为获取的id
//        Long empId = (Long) request.getSession().getAttribute("employee");
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);

//        在录入时有一个bug，如果添加的员工账号相同，那么就会报出错误
//        配置一个全局异常捕获器
        employeeService.save(employee);
        return R.success("新增员工成功");
    }

    /**
     * 添加一个员工信息分页查询
     * http://localhost:8080/employee/page?page=1&pageSize=10&name=zhangsan
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
//    请求为get类型传送数据
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page = {},pageSize={},name={}", page, pageSize, name);

//        1.构造分页构造器
//        默认配置成查第一页，一页十条 page=1&pageSize=10
        Page pageInfo = new Page(page, pageSize);
//        2.构造条件构建器
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<Employee>();
//        （1）添加过滤条件  StringUtils使用判断name是否为空，来选择是否添加这个like条件查询
        lqw.like(!StringUtils.isEmpty(name), Employee::getName, name);
//        （2）添加排序条件 根据修改时间来排序
        lqw.orderByDesc(Employee::getUpdateTime);
//        3.执行查询
        employeeService.page(pageInfo, lqw);
//        查询如果成功，则会自动的将对应查询完的数据封装到pageInfo中
        return R.success(pageInfo);
    }

    /**
     * 根据员工的id来进行对于员工信息的修改，一个通用方法
     * <p>
     * 因为为一个put请求所以需要进行添加putMapping注解
     * 请求网址:
     * http://localhost:8080/employee
     * 请求方法:
     * PUT
     *
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        log.info(employee.toString());

//        需要同步employee中的updateTime和updateUser字段
//        传入过来的id: 1666445557809401900, status: 0
//        数据库中的id：1666445557809401858
//        使用的是Long型数据，在js页面中只能保存16位，丢失精度，而数据库中为19位
//        需要对页面传过来的Long型数据进行转换，强转为String类型
//        Long empId = (Long) request.getSession().getAttribute("employee");
//        employee.setUpdateUser(empId);
//        employee.setUpdateTime(LocalDateTime.now());
        employeeService.updateById(employee);

        return R.success("员工信息修改成功");
    }

    @GetMapping("/{id}")
//    @GetMapping("/{id}")使用这种方式是因为在前端发送请求时
//    http://localhost:8080/employee/1666445557809401858 发送的是这种以id为请求路径的
//    所以在参数列表中的id上加上@PathVariable注解
    public R<Employee> getById(@PathVariable Long id) {
        log.info("查询员工信息");
        Employee employee = employeeService.getById(id);
        if (employee != null) {
            return R.success(employee);
        }
        return R.error("没有查询到对应员工信息");
    }
}
