package com.itsu.oa.controller;

import com.itsu.oa.core.model.R;
import com.itsu.oa.core.mvc.Auth;
import com.itsu.oa.util.ScriptRunner;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/process")
public class ProcessController {

    @Resource
    private ScriptRunner scriptRunner;

    @Auth
    @GetMapping("/list")
    public R list(int page, int limit,String search) {
        return R.success(scriptRunner.getRunningScripts());
    }
}
