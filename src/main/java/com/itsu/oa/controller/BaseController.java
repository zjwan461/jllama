package com.itsu.oa.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ShearCaptcha;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.itsu.oa.controller.req.RegisterReq;
import com.itsu.oa.core.exception.JException;
import com.itsu.oa.core.model.R;
import com.itsu.oa.core.mvc.Auth;
import com.itsu.oa.core.mvc.ServletContextHelper;
import com.itsu.oa.entity.SysInfo;
import com.itsu.oa.mapper.SysInfoMapper;
import com.itsu.oa.service.RegisterService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;

@RequestMapping("/api/base")
@RestController
public class BaseController {

    @Resource
    private SysInfoMapper sysInfoMapper;

    @Resource
    private RegisterService registerService;

    @Auth(requireLogin = false)
    @GetMapping("/sys-info")
    public R sysInfo() {
        SysInfo sysInfo = sysInfoMapper.selectOne(Wrappers.lambdaQuery(SysInfo.class).last("limit 1"));
        return R.success(sysInfo);
    }


    @Auth(requireLogin = false)
    @PostMapping("/register")
    public R register(@RequestBody RegisterReq registerReq) {
        SysInfo sysInfo = sysInfoMapper.selectOne(Wrappers.lambdaQuery(SysInfo.class).last("limit 1"));
        if (sysInfo != null) {
            throw new JException("系统已经注册");
        }
        registerService.register(registerReq);
        return R.success();
    }

    @Auth(requireLogin = false)
    @GetMapping("/yzm")
    public R yzm() throws IOException {
        ShearCaptcha captcha = CaptchaUtil.createShearCaptcha(100, 30, 4, 2);
        String code = captcha.getCode();
        ServletContextHelper.getSession().setAttribute("_CAPTCHA", code);
        return R.success(captcha.getImageBase64Data());
    }

    @Auth
    @GetMapping("/nav")
    public R nav() {
        String navStr = ResourceUtil.readUtf8Str("classpath:/data/nav.json");
        return R.success(JSONUtil.parseArray(navStr));
    }
}
