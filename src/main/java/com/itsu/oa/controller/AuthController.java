package com.itsu.oa.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.itsu.oa.controller.req.LoginReq;
import com.itsu.oa.core.event.LlammaShutdown;
import com.itsu.oa.core.exception.JException;
import com.itsu.oa.core.filter.AuthFilter;
import com.itsu.oa.core.model.R;
import com.itsu.oa.core.mvc.Auth;
import com.itsu.oa.core.mvc.ServletContextHelper;
import com.itsu.oa.entity.User;
import com.itsu.oa.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RequestMapping("/api/auth")
@RestController
public class AuthController {

    @Resource
    private UserService userService;

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    @PostMapping("/login")
    @Auth(requireLogin = false)
    public R login(@RequestBody LoginReq loginReq) {
        String code = (String) ServletContextHelper.getSession().getAttribute("_CAPTCHA");
        if (!StrUtil.equalsIgnoreCase(code, loginReq.getYzm())) {
            throw new JException("验证码错误");
        }
        User user = userService.getOne(Wrappers.lambdaQuery(User.class)
                .eq((SFunction<User, String>) User::getUsername, loginReq.getUsername())
                .eq((SFunction<User, String>) User::getPassword, SecureUtil.md5(loginReq.getPassword()))
        );

        if (user == null) {
            throw new JException("用户名或密码错误");
        }

        ServletContextHelper.getSession().setAttribute(AuthFilter.LOGIN_USER_KEY, user);
        return R.success();
    }

    @Auth
    @GetMapping("/logout")
    public R logout() {
        try {
            // 发布事件，通知所有正在运行的模型服务停止
            applicationEventPublisher.publishEvent(new LlammaShutdown("all"));
            // 清除用户登录信息并使会话失效
            ServletContextHelper.getSession().removeAttribute(AuthFilter.LOGIN_USER_KEY);
            ServletContextHelper.getSession().invalidate();
            return R.success("用户已成功登出");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // 增加异常处理，确保即使会话已失效也能正常返回
            return R.success("用户已登出");
        }
    }

}
