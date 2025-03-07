package com.itsu.oa.controller;

import com.itsu.oa.core.model.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/base")
public class BaseController {

    @GetMapping("/sys-info")
    public R sysInfo() {
        return R.success();
    }
}
