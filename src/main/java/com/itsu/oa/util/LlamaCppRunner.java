package com.itsu.oa.util;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.IOException;
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

        LLAMA_SERVER("llama-server");

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
                future.cancel(mayInterruptIfRunning);
                this.futures.remove(execId);
            } catch (Exception e) {
                throw new RuntimeException(e);
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
