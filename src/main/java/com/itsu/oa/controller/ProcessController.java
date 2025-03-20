package com.itsu.oa.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itsu.oa.config.JllamaConfigProperties;
import com.itsu.oa.controller.req.NewProcessReq;
import com.itsu.oa.core.exception.JException;
import com.itsu.oa.core.model.R;
import com.itsu.oa.core.mvc.Auth;
import com.itsu.oa.core.sys.GpuPlatform;
import com.itsu.oa.entity.*;
import com.itsu.oa.service.FileDownloadService;
import com.itsu.oa.service.LlamaExecHistService;
import com.itsu.oa.service.ModelService;
import com.itsu.oa.util.LlamaCppRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/process")
public class ProcessController {

    private static final Logger log = LoggerFactory.getLogger(ProcessController.class);

    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

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
                    List<String> split = StrUtil.split(x, "::");
                    map.put("modelName", split.get(0));
                    map.put("cppDir", split.get(1));
                    map.put("command", split.get(2));
                    map.put("args", split.get(3));
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
    public R create(@RequestBody NewProcessReq newProcessReq) throws Exception {
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
        if (StrUtil.isNotBlank(newProcessReq.getHost())) {
            argsBuilder.append("--host ").append(newProcessReq.getHost()).append(" ");
        }
        if (newProcessReq.getThreads() > 0) {
            argsBuilder.append("--threads ").append(newProcessReq.getThreads()).append(" ");
        }
        if (newProcessReq.getCtxSize() > 0) {
            argsBuilder.append("--ctx-size ").append(newProcessReq.getCtxSize()).append(" ");
        }
        if (newProcessReq.getParallel() > 0) {
            argsBuilder.append("--parallel ").append(newProcessReq.getParallel()).append(" ");
        }
        if (newProcessReq.getNgl() > 0) {
            argsBuilder.append("-ngl ").append(newProcessReq.getNgl()).append(" ");
        }
        argsBuilder.append("--log-file ").append(logFilePath);
        if (StrUtil.isNotBlank(newProcessReq.getArgs())) {
            argsBuilder.append(" ");
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
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        llamaExecHistService.save(entity);
        Future<LlamaCppRunner.LlamaCommandResp> future = llamaCppRunner.run(entity.getModelName(), entity.getLlamaCppDir(), LlamaCppRunner.LlamaCommand.match(entity.getLlamaCppCommand()),
                StrUtil.splitToArray(entity.getLlamaCppArgs(), " "));
        LlamaCppRunner.LlamaCommandResp llamaCommandResp = future.get();
        threadPoolTaskExecutor.submit(() -> {
            Process process = llamaCommandResp.getProcess();
            try {
                process.waitFor();
                int exitCode = process.exitValue();
                log.info("Exit Code: {}", exitCode);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread interrupted");
            } finally {
                process.destroy();
                future.cancel(true);
            }
        });
        return R.success();
    }


    @Auth
    @GetMapping("/list-history")
    public R listHistory(int page, int limit, String search) {
        Page<LlamaExecHis> res = llamaExecHistService.page(new Page<>(page, limit), Wrappers.lambdaQuery(LlamaExecHis.class)
                .orderByDesc((SFunction<LlamaExecHis, Date>) BaseEntity::getCreateTime)
                .like((SFunction<LlamaExecHis, String>) LlamaExecHis::getModelName, search)
                .or()
                .like((SFunction<LlamaExecHis, String>) LlamaExecHis::getFileName, search)
        );
        return R.success(res);
    }

    @Auth
    @DeleteMapping("/del-history/{id}")
    public R deleteHistory(@PathVariable Long id) {
        llamaExecHistService.removeById(id);
        return R.success();
    }

    @Auth
    @GetMapping("/log-history/{id}/{index}/{line}")
    public R logHistory(@PathVariable("id") Long id, @PathVariable("index") int index, @PathVariable("line") int line) {
        LlamaExecHis entity = llamaExecHistService.getById(id);
        if (entity == null) {
            throw new JException("不合法的参数");
        }
        // Assuming there's a method to get the log content based on the entity
        if (FileUtil.exist(entity.getLogFilePath())) {
            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(entity.getLogFilePath()))) {
                String currentLine = "";
                int currentIndex = 1;
                while ((currentLine = br.readLine()) != null) {
                    if (currentIndex >= index && currentIndex <= (index + line)) {
                        stringBuilder.append(currentLine).append("\n");
                    } else if (currentIndex > (index + line)) {
                        break;
                    }
                    currentIndex++;
                }
            } catch (IOException e) {
                throw new JException("读取文件失败");
            }

            return R.success(stringBuilder.toString());
        } else {
            throw new JException("日志文件不存在");
        }
    }
}