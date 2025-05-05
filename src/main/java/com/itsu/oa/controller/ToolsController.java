package com.itsu.oa.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itsu.oa.config.JllamaConfigProperties;
import com.itsu.oa.controller.req.ConvertReq;
import com.itsu.oa.controller.req.QuantizeReq;
import com.itsu.oa.controller.req.SplitMergeReq;
import com.itsu.oa.core.component.MessageQueue;
import com.itsu.oa.core.exception.JException;
import com.itsu.oa.core.model.R;
import com.itsu.oa.core.mvc.Auth;
import com.itsu.oa.entity.BaseEntity;
import com.itsu.oa.entity.GgufSplitMerge;
import com.itsu.oa.entity.ModelConvert;
import com.itsu.oa.entity.Quantize;
import com.itsu.oa.service.GgufSplitMergeService;
import com.itsu.oa.service.ModelConvertService;
import com.itsu.oa.service.QuantizeService;
import com.itsu.oa.service.SettingsService;
import com.itsu.oa.util.LlamaCppRunner;
import com.itsu.oa.util.ScriptRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Date;
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
    private QuantizeService quantizeService;

    @Resource
    private ThreadPoolTaskExecutor threadPool;

    @Resource
    private MessageQueue messageQueue;

    @Resource
    private JllamaConfigProperties jllamaConfigProperties;

    @Resource
    private ScriptRunner scriptRunner;

    @Resource
    private ModelConvertService modelConvertService;

    @Auth
    @GetMapping("/list-quantize-param")
    public R listQuantizeParam() {
        return R.success(jllamaConfigProperties.getQuantize().getSupportedTypes());
    }


    @Auth
    @GetMapping("/list-quantize")
    public R listQuantize(int page, int limit) {
        Page<Quantize> resPage = quantizeService.page(new Page<>(page, limit)
                , Wrappers.lambdaQuery(Quantize.class).orderByDesc((SFunction<Quantize, Date>) BaseEntity::getCreateTime));
        return R.success(resPage);
    }

    /**
     * llama-quantize <原模型目录> <转换后的模型保存路径> <量化位数>
     *
     * @return
     */
    @Auth
    @PostMapping("/llama-quantize")
    public R llamaQuantize(@RequestBody QuantizeReq quantizeReq) throws Exception {
        if (StrUtil.isBlank(quantizeReq.getOriginModel())) {
            throw new JException("量化源文件不能为空");
        }
        if (StrUtil.isBlank(quantizeReq.getQuantizeParam())) {
            throw new JException("量化精度参数不能为空");
        }
        if (!jllamaConfigProperties.getQuantize().getSupportedTypes().contains(quantizeReq.getQuantizeParam())) {
            throw new JException("不支持的量化精度参数：" + quantizeReq.getQuantizeParam());
        }
        if (StrUtil.isBlank(quantizeReq.getOutputModel())) {
            throw new JException("保存目录不能为空");
        }

        String llamaCppDir = settingsService.getCachedSettings().getLlamaCppDir();
        llamaCppRunner.runQuantize(llamaCppDir, quantizeReq.getOriginModel(), quantizeReq.getOutputModel(), quantizeReq.getQuantizeParam(), quantizeReq.isAsync());
        saveDB(quantizeReq);
        return R.success();
    }

    @Auth
    @GetMapping("/list-split-merge")
    public R list(int page, int limit) {
        IPage<GgufSplitMerge> resPage = ggufSplitMergeService.page(new Page<>(page, limit),
                Wrappers.lambdaQuery(GgufSplitMerge.class)
                        .orderByDesc((SFunction<GgufSplitMerge, Date>) BaseEntity::getCreateTime));
        return R.success(resPage);
    }

    @Auth
    @PostMapping("/split-merge")
    public R splitMerge(@RequestBody SplitMergeReq splitMergeReq) throws Exception {
        String options = splitMergeReq.getOptions();
        if ("split".equals(options)) {
            doSplit(splitMergeReq);
        } else if ("merge".equals(options)) {
            doMerge(splitMergeReq);
        } else
            throw new JException("不合法的请求参数options=" + options);
        return R.success();
    }

    private void doMerge(SplitMergeReq splitMergeReq) throws ExecutionException, InterruptedException {
        String input = splitMergeReq.getInput();
        String output = splitMergeReq.getOutput();
        if (StrUtil.isBlank(input)) {
            throw new JException("非法的合并参数,input源文件不能为空");
        }
        if (!StrUtil.contains(input, "-of-") || !"gguf".equals(FileUtil.getSuffix(input))) {
            throw new JException("非法的合并参数,input源文件格式不正确");
        }
        String llamaCppDir = settingsService.getCachedSettings().getLlamaCppDir();
        llamaCppRunner.runSplit(llamaCppDir, splitMergeReq.getOptions(), null, null, input, output, splitMergeReq.isAsync());
        saveDB(splitMergeReq);
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

        llamaCppRunner.runSplit(llamaCppDir, splitMergeReq.getOptions(), splitOption, splitParam, splitMergeReq.getInput(), splitMergeReq.getOutput(), splitMergeReq.isAsync());
        saveDB(splitMergeReq);
    }

    private void saveDB(QuantizeReq req) {
        Quantize quantize = new Quantize();
        quantize.setAsync(req.isAsync());
        quantize.setParam(req.getQuantizeParam());
        quantize.setInput(req.getOriginModel());
        quantize.setOutput(req.getOutputModel());
        quantizeService.save(quantize);
    }

    private void saveDB(SplitMergeReq splitMergeReq) {
        GgufSplitMerge entity = new GgufSplitMerge();
        entity.setInput(splitMergeReq.getInput());
        entity.setOutput(splitMergeReq.getOutput());
        entity.setOption(splitMergeReq.getOptions());
        entity.setSplitOption(splitMergeReq.getSplitOption());
        entity.setSplitParam(splitMergeReq.getSplitParam());
        entity.setAsync(splitMergeReq.isAsync());
        ggufSplitMergeService.save(entity);
    }

    @Auth
    @GetMapping("/script-list")
    public R scriptList() {
        File file = new File(System.getProperty("user.dir"), "scripts");
        String[] list = file.list((dir, name) -> name.startsWith("convert_") && name.endsWith(".py"));
        return R.success(list);
    }

    @Auth
    @PostMapping("/convert")
    public R convert(@RequestBody ConvertReq req) {
        String pyDir = settingsService.getCachedSettings().getPyDir();
        if (StrUtil.isBlank(pyDir) || !FileUtil.exist(pyDir)) {
            throw new JException("使用此功能需要先设置Python运行目录");
        }
        if (StrUtil.isBlank(req.getScriptFile())) {
            throw new JException("转换脚本必填");
        }
        if (StrUtil.isBlank(req.getInput())) {
            throw new JException("转换源目录必填");
        }
        if (!FileUtil.exist(req.getInput())) {
            throw new JException("转换源目录不存在");
        }
        if (StrUtil.isBlank(req.getOutput())) {
            throw new JException("输出文件目录必填");
        }
        if (!FileNameUtil.getSuffix(req.getOutput()).equals("gguf")) {
            throw new JException("输出文件格式必须是.gguf");
        }
        String execId = scriptRunner.runScript(pyDir + "/python", System.getProperty("user.dir") + "/scripts/" + req.getScriptFile(), req.isAsync(), req.getInput(), "--outfile", req.getOutput());
        ModelConvert modelConvert = new ModelConvert();
        modelConvert.setAsync(req.isAsync());
        modelConvert.setInput(req.getInput());
        modelConvert.setOutput(req.getOutput());
        modelConvert.setScriptFile(req.getScriptFile());
        modelConvertService.save(modelConvert);
        return R.success(execId);
    }

    @Auth
    @GetMapping("/convert-list")
    public R convertList(int page, int limit) {
        Page<ModelConvert> resPage = modelConvertService.page(new Page<>(page, limit), Wrappers.lambdaQuery(ModelConvert.class).orderByDesc(ModelConvert::getCreateTime));
        return R.success(resPage);
    }
}
