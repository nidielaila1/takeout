package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.reggie.common.R;
import com.reggie.entity.User;
import com.reggie.service.UserService;
import com.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 移动端用户发送验证码
     *
     * @param user
     * @param session
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMessage(@RequestBody User user, HttpSession session) {
//        获取手机号
        String phone = user.getPhone();
        if (!StringUtils.isEmpty(phone)) {
            //        生成随机四位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code = {}", code);
            //        调用阿里云api完成发送短信
//            SMSUtils.sendMessage("瑞吉外卖", "", phone, code);

            //        需要将生成的验证码保存到session中
//            session.setAttribute(phone, code);

//            将我们生成的验证码缓存到redis中，并设有效期为5分钟
            redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);

            return R.success("手机验证码短信发送成功");
        }
        return R.error("手机验证码短信发送失败");

    }

    /**
     * 移动端用户登录
     *
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {
        log.info("map = {}", map.toString());

//        获取手机号
        String phone = map.get("phone").toString();

//        获取验证码
        String code = map.get("code").toString();

//        Object codeInSession = session.getAttribute(phone);
//        从redis中获取验证码
        Object codeInRedis = redisTemplate.opsForValue().get(phone);


//        进行验证码比对（页面提交的验证码与Session中保存的验证码比对）
        if (codeInRedis != null && codeInRedis.equals(code)) {
//            如果比对成功那么说明登录成功
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);

            User user = userService.getOne(queryWrapper);

            if (user == null) {
//                判断当前用户的手机号是否为新用户，如果为新用户那么就自动完成注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
//            登录成功之后需要将userId放入到session中否则浏览器中没有
            session.setAttribute("user", user.getId());

//            如果用户登录成功，删除redis中的验证码
            redisTemplate.delete(phone);

            return R.success(user);
        }


        return R.error("登录失败");

    }


}
