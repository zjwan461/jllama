package com.itsu.jllama.controller;

import com.itsu.jllama.core.model.R;
import com.itsu.jllama.core.mvc.Auth;
import com.itsu.jllama.service.CheckUpdateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/check-update")
public class CheckUpdateController {

    @Resource
    private CheckUpdateService checkUpdateService;

    @Auth
    @GetMapping("/cpp")
    public R cppVersion() {
        return R.success(checkUpdateService.checkCppUpdate());
    }

    @Auth
    @GetMapping("/factory")
    public R factoryVersion() {
        return R.success(checkUpdateService.checkFactoryUpdate());
    }

    @Auth
    @GetMapping("/self")
    public R selfVersion() {
        return R.success(checkUpdateService.checkSelfUpdate());
    }

}
