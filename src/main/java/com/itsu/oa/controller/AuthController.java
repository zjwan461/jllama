package com.itsu.oa.controller;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.itsu.oa.core.exception.JException;
import com.itsu.oa.core.filter.AuthFilter;
import com.itsu.oa.core.model.R;
import com.itsu.oa.core.mvc.ServletContextHelper;
import com.itsu.oa.entity.User;
import com.itsu.oa.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RequestMapping("/api/auth")
@RestController
public class AuthController {

    @Resource
    private UserService userService;

    @PostMapping("/login")
    public R login(String username, String password) {
        User user = userService.getOne(Wrappers.lambdaQuery(User.class)
                .eq((SFunction<User, String>) User::getUsername, username)
                .eq((SFunction<User, String>) User::getPassword, SecureUtil.md5(password))
        );

        if (user == null) {
            throw new JException("用户名或密码错误");
        }

        ServletContextHelper.getSession().setAttribute(AuthFilter.LOGIN_USER_KEY, user);
        return R.success();
    }
}
