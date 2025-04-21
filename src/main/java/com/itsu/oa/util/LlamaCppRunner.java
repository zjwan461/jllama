package com.itsu.oa.util;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.itsu.oa.core.exception.JException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class LlamaCppRunner {

    private final ThreadPoolTaskExecutor threadPool;

    private final LinkedHashMap<String, LlamaCommandReq> futures = new LinkedHashMap<>();

    public LlamaCppRunner(ThreadPoolTaskExecutor threadPool) {
        this.threadPool = threadPool;
    }

    public enum LlamaCommand {

        LLAMA_SERVER("llama-server"),
        LLAMA_GGUF_SPLIT("llama-gguf-split"),
        LLAMA_QUANTIZE("llama-quantize");

        private final String command;

        LlamaCommand(String command) {
            this.command = command;
        }

        public String getCommand() {
            return command;
        }

        public static LlamaCommand match(String command) {
            if ("llama-server".equals(command)) {
                return LLAMA_SERVER;
            } else if ("llama-gguf-split".equals(command)) {
                return LLAMA_GGUF_SPLIT;
            } else if ("llama-quantize".equals(command)) {
                return LLAMA_QUANTIZE;
            }
            throw new IllegalArgumentException("No enum constant of " + command);
        }
    }

    public static class LlamaCommandReq {
        private String execId;
        private String command;
        private String cppDir;
        private String modelName;
        private String args;
        private Future<LlamaCommandResp> future;

        public String getExecId() {
            return execId;
        }

        public void setExecId(String execId) {
            this.execId = execId;
        }

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public String getCppDir() {
            return cppDir;
        }

        public void setCppDir(String cppDir) {
            this.cppDir = cppDir;
        }

        public String getModelName() {
            return modelName;
        }

        public void setModelName(String modelName) {
            this.modelName = modelName;
        }

        public String getArgs() {
            return args;
        }

        public void setArgs(String args) {
            this.args = args;
        }

        public Future<LlamaCommandResp> getFuture() {
            return future;
        }

        public void setFuture(Future<LlamaCommandResp> future) {
            this.future = future;
        }
    }

    public static class LlamaCommandResp {

        private String execId;

        private String modelName;

        private Process process;

        private Date createTime;

        public String getExecId() {
            return execId;
        }

        public void setExecId(String execId) {
            this.execId = execId;
        }

        public Process getProcess() {
            return process;
        }

        public void setProcess(Process process) {
            this.process = process;
        }

        public Date getCreateTime() {
            return createTime;
        }

        public void setCreateTime(Date createTime) {
            this.createTime = createTime;
        }

        public String getModelName() {
            return modelName;
        }

        public void setModelName(String modelName) {
            this.modelName = modelName;
        }
    }

    public LlamaCommandReq runQuantize(String cppDir, String input, String output, String quantizeParam, boolean async) {
        String execId = IdUtil.fastSimpleUUID();
        LlamaCommandReq llamaCommandReq = new LlamaCommandReq();
        llamaCommandReq.setExecId(execId);
        llamaCommandReq.setCommand(LlamaCommand.LLAMA_QUANTIZE.getCommand());
        llamaCommandReq.setCppDir(cppDir);
        LlamaCommandResp llamaCommandResp = new LlamaCommandResp();
        llamaCommandResp.setExecId(execId);
        llamaCommandResp.setCreateTime(new Date());
        List<String> commandList = new ArrayList<>();
        commandList.add(cppDir + "/" + LlamaCommand.LLAMA_QUANTIZE.getCommand());
        commandList.add(input);
        commandList.add(output);
        commandList.add(quantizeParam);
        if (async) {
            Future<LlamaCommandResp> future = threadPool.submit(() -> {
                try {
                    ProcessBuilder processBuilder = new ProcessBuilder(commandList);
                    // 启动进程
                    Process process = processBuilder.start();

                    llamaCommandResp.setProcess(process);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
                return llamaCommandResp;
            });
            llamaCommandReq.setFuture(future);
            futures.put(execId, llamaCommandReq);
        } else {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder(commandList);
                // 启动进程
                Process process = processBuilder.start();
                logProcessOutput(process, LlamaCommand.LLAMA_QUANTIZE);
                int result = process.waitFor();
                if (result != 0) {
                    log.error("llama-quantize 执行失败,return code={}", result);
                    throw new JException("llama-quantize 模型量化执行失败, return code=" + result);
                }
            } catch (JException e) {
                throw e;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new JException("模型量化操作失败");
            }
        }

        return llamaCommandReq;
    }

    public LlamaCommandReq runSplit(String cppDir, String option, String splitOption, String splitParam, String input, String output, boolean async) {
        String execId = IdUtil.fastSimpleUUID();
        LlamaCommandReq llamaCommandReq = new LlamaCommandReq();
        llamaCommandReq.setExecId(execId);
        llamaCommandReq.setCommand(LlamaCommand.LLAMA_GGUF_SPLIT.getCommand());
        llamaCommandReq.setCppDir(cppDir);
        LlamaCommandResp llamaCommandResp = new LlamaCommandResp();
        llamaCommandResp.setExecId(execId);
        llamaCommandResp.setCreateTime(new Date());
        List<String> commandList = new ArrayList<>();
        commandList.add(cppDir + "/" + LlamaCommand.LLAMA_GGUF_SPLIT.getCommand());
        commandList.add("--" + option);
        if (StrUtil.isNotBlank(splitOption) && StrUtil.isNotBlank(splitParam)) {
            commandList.add("--" + splitOption);
            commandList.add(splitParam);
        }
        commandList.add(input);
        commandList.add(output);
        if (async) {
            Future<LlamaCommandResp> future = threadPool.submit(() -> {
                try {
                    ProcessBuilder processBuilder = new ProcessBuilder(commandList);
                    // 启动进程
                    Process process = processBuilder.start();

                    llamaCommandResp.setProcess(process);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
                return llamaCommandResp;
            });
            llamaCommandReq.setFuture(future);
            futures.put(execId, llamaCommandReq);
        } else {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder(commandList);
                // 启动进程
                Process process = processBuilder.start();
                logProcessOutput(process, LlamaCommand.LLAMA_GGUF_SPLIT);
                int result = process.waitFor();
                if (result != 0) {
                    log.error("llama-split 执行失败,return code={}", result);
                    throw new JException("llama-split 执行失败, return code=" + result);
                }
            } catch (JException e) {
                throw e;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new JException("拆分/合并操作失败");
            }
        }
        return llamaCommandReq;
    }

    public void logProcessOutput(Process process, LlamaCommand command) {
        InputStream is = process.getInputStream();
        InputStream es = process.getErrorStream();
        threadPool.submit(() -> {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line = "";
                while ((line = br.readLine()) != null) {
                    log.info("{}:{}", command.getCommand(), line);
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        });
        threadPool.submit(() -> {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(es))) {
                String line = "";
                while ((line = br.readLine()) != null) {
                    //测试了一下llama.cpp一些输出会通过errorStream输出，因此改用info记录
                    log.info("{}:{}", command.getCommand(), line);
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    public LlamaCommandReq run(String modelName, String cppDir, LlamaCommand command, String... args) {
        String execId = IdUtil.fastSimpleUUID();
        LlamaCommandReq llamaCommandReq = new LlamaCommandReq();
        llamaCommandReq.setExecId(execId);
        llamaCommandReq.setCommand(command.getCommand());
        llamaCommandReq.setCppDir(cppDir);
        llamaCommandReq.setModelName(modelName);
        llamaCommandReq.setArgs(JSONUtil.toJsonStr(args));
        Future<LlamaCommandResp> future = threadPool.submit(() -> {
            LlamaCommandResp llamaCommandResp = new LlamaCommandResp();
            llamaCommandResp.setExecId(execId);
            llamaCommandResp.setCreateTime(new Date());
            llamaCommandResp.setModelName(modelName);
            try {
                List<String> commandList = new ArrayList<>();
                commandList.add(cppDir + "/" + command.getCommand());
                if (args != null && args.length > 0) {
                    commandList.addAll(Arrays.asList(args));
                }
                // 创建 ProcessBuilder 对象，指定要执行的命令
                ProcessBuilder processBuilder = new ProcessBuilder(commandList);
                // 启动进程
                Process process = processBuilder.start();

                llamaCommandResp.setProcess(process);
                // 获取脚本执行的输出流
//                BufferedReader infoReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//                String line = "";
//                while ((line = infoReader.readLine()) != null) {
//                    log.info("script output:{}", line);
//                }
//
//                // 等待脚本执行完成并获取返回值
//                int exitCode = process.waitFor();
//                System.out.println("脚本执行完成，返回值: " + exitCode);
//                if (exitCode != 0) {
//                    String error;
//                    while ((error = errorReader.readLine()) != null) {
//                        log.error("script output:{}", error);
//                    }
//                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
            return llamaCommandResp;
        });
        llamaCommandReq.setFuture(future);
        futures.put(execId, llamaCommandReq);
        return llamaCommandReq;
    }


    public void stop(String execId, boolean mayInterruptIfRunning) {
        LlamaCommandReq llamaCommandReq = this.futures.get(execId);
        if (llamaCommandReq != null) {
            Future<LlamaCommandResp> future = llamaCommandReq.getFuture();
            try {
                LlamaCommandResp llamaCommandResp = future.get();
                llamaCommandResp.getProcess().destroy();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                future.cancel(mayInterruptIfRunning);
                this.futures.remove(execId);
            }

        }
    }

    public void stopAll() {
        if (!this.futures.isEmpty()) {
            for (LlamaCommandReq llamaCommandReq : this.futures.values()) {
                Future<LlamaCommandResp> future = llamaCommandReq.getFuture();
                try {
                    LlamaCommandResp llamaCommandResp = future.get();
                    llamaCommandResp.getProcess().destroy();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                future.cancel(true);
                this.futures.remove(llamaCommandReq.getExecId());
            }
        }
    }

    public Collection<LlamaCommandReq> getRunningService() {
        return this.futures.values();
    }

    public Future<LlamaCommandResp> getRunningServiceFuture(String id) {
        if (this.futures.containsKey(id)) {
            return this.futures.get(id).getFuture();
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        threadPoolTaskExecutor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        threadPoolTaskExecutor.setQueueCapacity(10000);
        threadPoolTaskExecutor.initialize();
        LlamaCppRunner llamaCppRunner = new LlamaCppRunner(threadPoolTaskExecutor);
        LlamaCommandReq llamaCommandReq = llamaCppRunner.run("unsloth/DeepSeek-R1-Distill-Qwen-1.5B-GGUF", "E:\\workspaces\\java\\jllama\\llama\\llama-b4893-bin-win-avx-x64", LlamaCommand.LLAMA_SERVER, "--model", "D:\\models\\modelScope\\unsloth\\DeepSeek-R1-Distill-Qwen-1.5B-GGUF\\DeepSeek-R1-Distill-Qwen-1.5B-Q4_K_M.gguf", "--port", "8000", "--log-file", "1.log");
        Future<LlamaCommandResp> future = llamaCommandReq.getFuture();
        LlamaCommandResp llamaCommandResp = future.get();
        TimeUnit.MINUTES.sleep(2);
//        Process process = llamaCommandResp.getProcess();
//        process.destroy();
        llamaCppRunner.stop(llamaCommandResp.getExecId(), true);
        threadPoolTaskExecutor.shutdown();
    }
}
