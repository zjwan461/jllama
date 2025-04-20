package com.itsu.oa.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.itsu.oa.controller.req.SplitMergeReq;
import com.itsu.oa.core.exception.JException;
import com.itsu.oa.core.model.R;
import com.itsu.oa.core.mvc.Auth;
import com.itsu.oa.service.SettingsService;
import com.itsu.oa.util.LlamaCppRunner;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author jerry.su
 * @date 2025/4/20 13:37
 */
@RestController
@RequestMapping("/api/tools")
public class ToolsController {

    @Resource
    private LlamaCppRunner llamaCppRunner;

    @Resource
    private HttpServletRequest request;

    @Resource
    private SettingsService settingsService;

    @Auth
    @PostMapping("/split-merge")
    public R splitMerge(@RequestBody SplitMergeReq splitMergeReq) {
        String options = splitMergeReq.getOptions();
        if ("split".equals(options)) {
            doSplit(splitMergeReq);
        }
        return R.success();
    }

    private void doSplit(SplitMergeReq splitMergeReq) {
        String input = splitMergeReq.getInput();
        String splitOption = splitMergeReq.getSplitOption();
        String splitParam = splitMergeReq.getSplitParam();
        if (StrUtil.isBlank(input)) {
            throw new JException("输入文件不能为空");
        }
        if (!FileUtil.exist(input)) {
            throw new JException("输入文件不存在");
        }
        if (!FileUtil.getSuffix(input).equals("gguf")) {
            throw new JException("输入文件不是gguf文件");
        }
        if ("split-max-tensors".equals(splitOption)) {
            if (!NumberUtil.isNumber(splitParam)) {
                throw new JException("非法的拆分参数split-max-tensors：" + splitParam + ",应为数字");
            }
        } else if ("split-max-size".equals(splitOption)) {
            if (StrUtil.isBlank(splitParam)) {
                throw new JException("非法的拆分参数split-max-size：" + splitParam + ",应为非空字符串");
            }
            String num = StrUtil.sub(splitParam, 0, splitParam.length() - 1);
            if (!NumberUtil.isNumber(num)) {
                throw new JException("非法的拆分参数split-max-size：" + splitParam + ",应为数字加G或M");
            }
            String lastLetter = StrUtil.sub(splitParam, splitParam.length() - 1, splitParam.length());
            if (!"G".equals(lastLetter) && !"M".equals(lastLetter)) {
                throw new JException("非法的拆分参数split-max-size：" + splitParam + ",应为数字加G或M");
            }
        } else
            throw new JException("非法的拆分参数：" + splitOption);

        String llamaCppDir = settingsService.getCachedSettings().getLlamaCppDir();
        llamaCppRunner.runSplit(llamaCppDir, splitMergeReq.getOptions(), splitOption, splitParam, splitMergeReq.getInput(), splitMergeReq.getOutput(), splitMergeReq.isAsync());

    }
}
