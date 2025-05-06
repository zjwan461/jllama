package com.itsu.jllama.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ShearCaptcha;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.system.SystemUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.itsu.jllama.controller.req.RegisterReq;
import com.itsu.jllama.core.exception.JException;
import com.itsu.jllama.core.model.R;
import com.itsu.jllama.core.mvc.Auth;
import com.itsu.jllama.core.mvc.ServletContextHelper;
import com.itsu.jllama.core.sys.Platform;
import com.itsu.jllama.entity.Settings;
import com.itsu.jllama.entity.SysInfo;
import com.itsu.jllama.mapper.SysInfoMapper;
import com.itsu.jllama.service.RegisterService;
import com.itsu.jllama.service.SettingsService;
import com.itsu.jllama.util.ScriptRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequestMapping("/api/base")
@RestController
public class BaseController {

    @Resource
    private SysInfoMapper sysInfoMapper;

    @Resource
    private RegisterService registerService;

    @Resource
    private SettingsService settingsService;

    @Resource
    private ScriptRunner scriptRunner;

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

    @Auth
    @GetMapping("/settings")
    public R settings() {
        Settings settings = settingsService.getById(Settings.DEFAULT_ID);
        return R.success(settings);
    }

    @Auth
    @PostMapping("/update-settings")
    public R updateSettings(@RequestBody Settings settings) {
        checkSettingsAndUpdateSysInfo(settings);
        settings.setId(Settings.DEFAULT_ID);
        settingsService.updateCachedSettings(settings);
        return R.success();
    }

    private void checkSettingsAndUpdateSysInfo(Settings settings) {
        String llamaCppDir = settings.getLlamaCppDir();
        if (!FileUtil.exist(llamaCppDir)) {
            throw new JException("llama.cpp目录不存在");
        }
        if (!FileUtil.exist(llamaCppDir, "llama-server.*")) {
            throw new JException("llama.cpp目录结构非法或已被破坏");
        }
        String pyDir = settings.getPyDir();
        if (!FileUtil.exist(pyDir)) {
            throw new JException("python运行环境目录不存在");
        }
        if (!FileUtil.exist(pyDir, "python.*")) {
            throw new JException("python运行环境目录结构非法或已被破坏");
        }
        String verStr = StrUtil.trim(scriptRunner.runScriptAndRead(llamaCppDir + "/llama-server", "--version", false, true));
        System.out.println(verStr);
        if (StrUtil.isBlank(verStr)) {
            throw new JException("非法的llama.cpp运行目录");
        }
        String cppVersion = verStr.substring("version:".length(), verStr.indexOf("(")).trim();
        String osName = SystemUtil.getOsInfo().getName();
        Platform platform = Platform.match(osName);
        String factoryVersion = null;
        if (platform == Platform.WINDOWS) {
            factoryVersion = StrUtil.trim(scriptRunner.runScriptAndRead(pyDir + "/Scripts/llamafactory-cli", "version", true, false));
        } else {
            factoryVersion = StrUtil.trim(scriptRunner.runScriptAndRead(pyDir + "/bin/llamafactory-cli", "version", true, false));
        }

        if (StrUtil.isBlank(factoryVersion)) {
            throw new JException("非法的python运行环境目录");
        }

        Pattern pattern = Pattern.compile("[0-9].[0-9].[0-9]");
        Matcher matcher = pattern.matcher(factoryVersion);
        if (matcher.find()) {
            factoryVersion = matcher.group();
        } else {
            log.error("cant not found LlamaFactory version");
            factoryVersion = null;
        }

        SysInfo sysInfo = SpringUtil.getBean(SysInfo.class);
        sysInfo.setCppVersion(cppVersion);
        sysInfo.setFactoryVersion(factoryVersion);
        sysInfo.setUpdateTime(new Date());
        sysInfoMapper.updateById(sysInfo);

    }

//    @Auth
//    @GetMapping("/tools-md")
//    public R toolsMd() {
//        String mdStr = ResourceUtil.readUtf8Str("classpath:/data/tools.md");
//        return R.success(mdStr);
//    }
}
