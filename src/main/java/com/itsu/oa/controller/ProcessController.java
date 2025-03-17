package com.itsu.oa.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.itsu.oa.core.model.R;
import com.itsu.oa.core.mvc.Auth;
import com.itsu.oa.util.LlamaCppRunner;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/process")
public class ProcessController {

    @Resource
    private LlamaCppRunner llamaCppRunner;

    @Auth
    @GetMapping("/list")
    public R list(int page, int limit, String search) {
        Set<String> runningService = llamaCppRunner.getRunningService();
        List<Map> list = runningService.stream()
                .filter(x -> x.contains(search))
                .map(x -> {
                    Map map = new HashMap();
                    List<String> split = StrUtil.split(x, ":");
                    return map;
                })
                .collect(Collectors.toList());
        int total = list.size();
        int index = (page - 1) * limit;
        List<Map> records = new ArrayList<>();
        Map data = new HashMap();
        data.put("total", total);
        data.put("current", page);
        if (index > total) {
        } else {
            if (index + limit > total) {
                records = CollUtil.sub(list, index, total);
            } else {
                records = CollUtil.sub(list, index, index + limit);
            }
        }
        data.put("records", records);
        data.put("size", records.size());
        return R.success(data);
    }


    @Auth
    @GetMapping("/list-command")
    public R listCommand() {
        return R.success(LlamaCppRunner.LlamaCommand.values());
    }
}
