package com.itsu.oa.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.itsu.oa.config.JllamaConfigProperties;
import com.itsu.oa.controller.req.NewProcessReq;
import com.itsu.oa.core.exception.JException;
import com.itsu.oa.core.model.R;
import com.itsu.oa.core.mvc.Auth;
import com.itsu.oa.core.sys.GpuPlatform;
import com.itsu.oa.entity.FileDownload;
import com.itsu.oa.entity.LlamaExecHis;
import com.itsu.oa.entity.Model;
import com.itsu.oa.entity.SysInfo;
import com.itsu.oa.service.FileDownloadService;
import com.itsu.oa.service.LlamaExecHistService;
import com.itsu.oa.service.ModelService;
import com.itsu.oa.util.LlamaCppRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/process")
public class ProcessController {

    @Resource
    private LlamaCppRunner llamaCppRunner;

    @Resource
    private LlamaExecHistService llamaExecHistService;

    @Resource
    private ModelService modelService;

    @Resource
    private FileDownloadService fileDownloadService;

    @Resource
    @Lazy
    private SysInfo sysInfo;

    @Resource
    private JllamaConfigProperties jllamaConfigProperties;

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

    @Auth
    @PostMapping("/create")
    public R create(@RequestBody NewProcessReq newProcessReq) {
        Model model = modelService.getById(newProcessReq.getModelId());
        if (model == null) {
            throw new JException("不合法的model id");
        }
        FileDownload fileDownload = fileDownloadService.getById(newProcessReq.getFileId());
        if (fileDownload == null) {
            throw new JException("不合法的file id");
        }
        LlamaExecHis entity = new LlamaExecHis();
        entity.setModelId(model.getId());
        entity.setModelName(model.getName());
        entity.setFileId(fileDownload.getId());
        entity.setFileName(fileDownload.getFileName());
        entity.setFilePath(fileDownload.getFilePath());
        if (jllamaConfigProperties.getGpu().isEnable()) {
            if (GpuPlatform.CPU.name().equals(sysInfo.getGpuPlatform())) {
                entity.setLlamaCppDir(jllamaConfigProperties.getLlamaCpuDir());
            } else if (GpuPlatform.CUDA.name().equals(sysInfo.getGpuPlatform())) {
                entity.setLlamaCppDir(jllamaConfigProperties.getGpu().getLlamaDir());
            }
        } else {
            entity.setLlamaCppDir(jllamaConfigProperties.getLlamaCpuDir());
        }
        String logFilePath = jllamaConfigProperties.getLlamaLogDir() + "/" + newProcessReq.getLlamaCommand().getCommand() + "-" + DateUtil.current() + ".log";
        entity.setLogFilePath(logFilePath);
        StringBuilder argsBuilder = new StringBuilder();
        argsBuilder.append("--model ").append(fileDownload.getFilePath() + "/" + fileDownload.getFileName()).append(" ");
        argsBuilder.append("--port ").append(newProcessReq.getPort()).append(" ");
        argsBuilder.append("--threads ").append(newProcessReq.getThreads()).append(" ");
        argsBuilder.append("--ctx-size ").append(newProcessReq.getCtxSize()).append(" ");
        argsBuilder.append("--parallel ").append(newProcessReq.getParallel()).append(" ");
        argsBuilder.append("-ngl ").append(newProcessReq.getNgl()).append(" ");
        argsBuilder.append("--log-file ").append(logFilePath).append(" ");
        if (StrUtil.isNotBlank(newProcessReq.getArgs())) {
            List<String> argList = StrUtil.split(newProcessReq.getArgs(), " ");
            for (String item : argList) {
                if (item.contains("-")) {
                    item += " "; // Add a space after the dash to ensure correct argument parsing
                }
                argsBuilder.append(item);
            }
        }
        entity.setLlamaCppArgs(argsBuilder.toString());
        entity.setLlamaCppCommand(newProcessReq.getLlamaCommand().getCommand());
        entity.setStatus(0);
        entity.setLlamaCppArgs(newProcessReq.getArgs());
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        llamaExecHistService.save(entity);
        return R.success();
    }


}