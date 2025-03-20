package com.itsu.oa.util;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class LlamaCppRunner {

    private final ThreadPoolTaskExecutor threadPool;

    private final LinkedHashMap<String, Future<LlamaCommandResp>> futures = new LinkedHashMap<>();

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

    public static class LlamaCommandResp {

        private String modelName;

        private String commandScheduleKey;

        private Process process;

        private Date createTime;

        public String getCommandScheduleKey() {
            return commandScheduleKey;
        }

        public void setCommandScheduleKey(String commandScheduleKey) {
            this.commandScheduleKey = commandScheduleKey;
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

    public Future<LlamaCommandResp> run(String modelName, String cppDir, LlamaCommand command, String... args) {
        String execId = IdUtil.fastSimpleUUID();
        String scheduleKey = generateScheduleKey(execId, modelName, cppDir, command.getCommand(), args);
        if (this.futures.containsKey(scheduleKey)) {
            log.info("当前脚本：{}运行中", scheduleKey);
            return this.futures.get(scheduleKey);
        }

        Future<LlamaCommandResp> future = threadPool.submit(() -> {
            LlamaCommandResp llamaCommandResp = new LlamaCommandResp();
            llamaCommandResp.setModelName(modelName);
            llamaCommandResp.setCommandScheduleKey(scheduleKey);
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
                log.error(e.getMessage());
            }
            return llamaCommandResp;
        });

        futures.put(generateScheduleKey(execId, modelName, cppDir, command.getCommand(), args), future);
        return future;
    }


    public void stop(String scheduleKey, boolean mayInterruptIfRunning) {
        if (this.futures.containsKey(scheduleKey)) {
            Future<LlamaCommandResp> future = futures.get(scheduleKey);
            try {
                LlamaCommandResp llamaCommandResp = future.get();
                llamaCommandResp.getProcess().destroy();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            future.cancel(mayInterruptIfRunning);
            this.futures.remove(scheduleKey);
        }
    }

    public void stopById(String execId, boolean mayInterruptIfRunning) {
        this.futures.keySet().stream().filter(x -> x.startsWith(execId)).forEach(x -> stop(x, mayInterruptIfRunning));
    }

    public String generateScheduleKey(String execId, String modelName, String cppDir, String command, String... args) {
        return execId + "::" + modelName + "::" + cppDir + "::" + command + "::" + JSONUtil.toJsonStr(args);
    }


    public void stopAll() {
        if (!this.futures.isEmpty()) {
            for (Future<LlamaCommandResp> future : this.futures.values()) {
                try {
                    LlamaCommandResp llamaCommandResp = future.get();
                    llamaCommandResp.getProcess().destroy();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                future.cancel(true);
            }
        }
    }

    public Set<String> getRunningService() {
        Set<String> runningScripts = new LinkedHashSet<>();
        if (MapUtil.isNotEmpty(this.futures)) {
            return this.futures.keySet();
        }

        return runningScripts;
    }

    public Future<LlamaCommandResp> getRunningServicetFuture(String scheduleKey) {
        if (this.futures.containsKey(scheduleKey)) {
            return this.futures.get(scheduleKey);
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
        Future<LlamaCommandResp> future = llamaCppRunner.run("unsloth/DeepSeek-R1-Distill-Qwen-1.5B-GGUF", "E:\\workspaces\\java\\jllama\\llama\\llama-b4893-bin-win-avx-x64", LlamaCommand.LLAMA_SERVER, "--model", "D:\\models\\modelScope\\unsloth\\DeepSeek-R1-Distill-Qwen-1.5B-GGUF\\DeepSeek-R1-Distill-Qwen-1.5B-Q4_K_M.gguf", "--port", "8000", "--log-file", "1.log");
        LlamaCommandResp llamaCommandResp = future.get();
        TimeUnit.MINUTES.sleep(2);
//        Process process = llamaCommandResp.getProcess();
//        process.destroy();
        llamaCppRunner.stop(llamaCommandResp.getCommandScheduleKey(), true);
        threadPoolTaskExecutor.shutdown();
    }
}
