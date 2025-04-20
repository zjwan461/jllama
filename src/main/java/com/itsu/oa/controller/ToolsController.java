package com.itsu.oa.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.itsu.oa.controller.req.SplitMergeReq;
import com.itsu.oa.core.component.MessageQueue;
import com.itsu.oa.core.component.Msg;
import com.itsu.oa.core.exception.JException;
import com.itsu.oa.core.model.R;
import com.itsu.oa.core.mvc.Auth;
import com.itsu.oa.entity.GgufSplitMerge;
import com.itsu.oa.service.GgufSplitMergeService;
import com.itsu.oa.service.SettingsService;
import com.itsu.oa.util.LlamaCppRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ExecutionException;

/**
 * @author jerry.su
 * @date 2025/4/20 13:37
 */
@RestController
@RequestMapping("/api/tools")
@Slf4j
public class ToolsController {

    @Resource
    private LlamaCppRunner llamaCppRunner;

    @Resource
    private HttpServletRequest request;

    @Resource
    private SettingsService settingsService;

    @Resource
    private GgufSplitMergeService ggufSplitMergeService;

    @Resource
    private ThreadPoolTaskExecutor threadPool;

    @Resource
    private MessageQueue messageQueue;

    @Auth
    @PostMapping("/split-merge")
    public R splitMerge(@RequestBody SplitMergeReq splitMergeReq) throws Exception {
        String options = splitMergeReq.getOptions();
        if ("split".equals(options)) {
            doSplit(splitMergeReq);
        }
        //todo merge
        return R.success();
    }

    private void doSplit(SplitMergeReq splitMergeReq) throws ExecutionException, InterruptedException {
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

        LlamaCppRunner.LlamaCommandReq llamaCommandReq = llamaCppRunner.runSplit(llamaCppDir, splitMergeReq.getOptions(), splitOption, splitParam, splitMergeReq.getInput(), splitMergeReq.getOutput(), splitMergeReq.isAsync());
        if (llamaCommandReq.getFuture() != null) {
            LlamaCppRunner.LlamaCommandResp llamaCommandResp = llamaCommandReq.getFuture().get();
            threadPool.submit(() -> {
                Msg msg = new Msg();
                msg.setTitle("模型" + splitMergeReq.getOptions());

                try {
                    Process process = llamaCommandResp.getProcess();
                    process.waitFor();
                    if (process.exitValue() == 0) {
                        msg.setContent("模型" + splitMergeReq.getOptions() + "成功");
                        msg.setStatus(Msg.Status.success);
                        log.info("execID: {} process Exit Code: {}", llamaCommandReq.getExecId(), process.exitValue());
                    } else {
                        msg.setContent("模型" + splitMergeReq.getOptions() + "失败");
                        msg.setStatus(Msg.Status.error);
                        log.error("execID: {} process Exit Code: {}", llamaCommandReq.getExecId(), process.exitValue());
                    }
                } catch (InterruptedException e) {
                    msg.setContent("模型" + splitMergeReq.getOptions() + "失败");
                    msg.setStatus(Msg.Status.error);
                    Thread.currentThread().interrupt();
                    log.error("execID: {} Thread interrupted", llamaCommandReq.getExecId(), e);
                } finally {
                    llamaCppRunner.stop(llamaCommandReq.getExecId(), true);
                    try {
                        messageQueue.put(msg);
                    } catch (InterruptedException e) {
                        log.error(e.getMessage());
                    }
                }
            });
        }
        GgufSplitMerge entity = new GgufSplitMerge();
        entity.setInput(splitMergeReq.getInput());
        entity.setOutput(splitMergeReq.getOutput());
        entity.setOption(splitMergeReq.getOptions());
        entity.setSplitOption(splitMergeReq.getSplitOption());
        entity.setSplitParam(splitMergeReq.getSplitParam());
        entity.setAsync(splitMergeReq.isAsync());
        ggufSplitMergeService.save(entity);
    }
}
