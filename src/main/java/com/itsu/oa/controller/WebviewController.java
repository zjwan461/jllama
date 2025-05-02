package com.itsu.oa.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.itsu.oa.core.model.R;
import com.itsu.oa.core.mvc.Auth;
import com.itsu.oa.core.sys.Platform;
import com.itsu.oa.entity.SysInfo;
import com.itsu.oa.mapper.SysInfoMapper;
import com.itsu.oa.util.SysUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/webview")
public class WebviewController {
    private final SysInfoMapper sysInfoMapper;

    private final SysUtil sysUtil;

    public WebviewController(SysInfoMapper sysInfoMapper, SysUtil sysUtil) {
        this.sysInfoMapper = sysInfoMapper;
        this.sysUtil = sysUtil;
    }

    @Auth(requireLogin = false)
    @GetMapping("/browser")
    public R browser(String url) {
        SysInfo sysInfo = sysInfoMapper.selectOne(Wrappers.lambdaQuery(SysInfo.class).last("limit 1"));
        String platform = sysInfo.getPlatform();
        Platform plat = Platform.match(platform);
        System.out.println(plat);
        if (plat == Platform.WINDOWS) {
            sysUtil.openBrowser(url, "cmd", "/c", "start");
        } else if (plat == Platform.MAC) {
            sysUtil.openBrowser(url, "bash", "open");
        } else {
            log.info("当前操作系统：{}不支持打开浏览器", plat);
        }
        return R.success();
    }

}
