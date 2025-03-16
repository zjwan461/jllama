package com.itsu.oa.util;

import cn.hutool.core.map.MapUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.Future;

@Slf4j
@Component
public class ScriptRunner {

    private final ThreadPoolTaskExecutor threadPool;

    private LinkedHashMap<String, Future<ScriptResp>> futures = new LinkedHashMap<>();

    public ScriptRunner(ThreadPoolTaskExecutor threadPool) {
        this.threadPool = threadPool;
    }

    public enum SCRIPT_TYPE {
        BASH("/bin/bash"),
        PYTHON("python3"),
        BAT("cmd.exe", "/c");

        private final String[] executor;

        SCRIPT_TYPE(String... executor) {
            this.executor = executor;
        }

        public String[] getExecutor() {
            return executor;
        }
    }

    public static class ScriptResp {
        private int code;
        private String script;
        private SCRIPT_TYPE scriptType;
        private String[] args;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getScript() {
            return script;
        }

        public void setScript(String script) {
            this.script = script;
        }

        public SCRIPT_TYPE getScriptType() {
            return scriptType;
        }

        public void setScriptType(SCRIPT_TYPE scriptType) {
            this.scriptType = scriptType;
        }

        public String[] getArgs() {
            return args;
        }

        public void setArgs(String[] args) {
            this.args = args;
        }


        @Override
        public String toString() {
            return "ScriptResp{" +
                    "code=" + code +
                    ", script='" + script + '\'' +
                    ", scriptType=" + scriptType +
                    ", args=" + Arrays.toString(args) +
                    '}';
        }
    }


    public Future<ScriptResp> runScript(String script, SCRIPT_TYPE scriptType, String... args) {
        String scheduleKey = generateScheduleKey(script, args);
        if (this.futures.containsKey(scheduleKey)) {
            log.info("当前脚本：{}运行中", scheduleKey);
            return this.futures.get(scheduleKey);
        }
        ScriptResp scriptResp = new ScriptResp();
        scriptResp.setScript(script);
        scriptResp.setScriptType(scriptType);
        scriptResp.setArgs(args);
        Future<ScriptResp> future = threadPool.submit(() -> {
            try {
                List<String> commandList = new ArrayList<>();
                commandList.addAll(Arrays.asList(scriptType.getExecutor()));
                commandList.add(script);
                if (args != null && args.length > 0) {
                    commandList.addAll(Arrays.asList(args));
                }
                // 创建 ProcessBuilder 对象，指定要执行的命令
                ProcessBuilder processBuilder = new ProcessBuilder(commandList);
                // 启动进程
                Process process = processBuilder.start();

                // 获取脚本执行的输出流
                BufferedReader infoReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                int len;
                char[] cache = new char[1024];
                while ((len = infoReader.read(cache)) != -1) {
                    String tmp = new String(cache, 0, len);
                    log.info("script output:{}", tmp);
                }

                // 等待脚本执行完成并获取返回值
                int exitCode = process.waitFor();
                System.out.println("脚本执行完成，返回值: " + exitCode);
                scriptResp.setCode(exitCode);
                if (exitCode != 0) {
                    String error;
                    while ((error = errorReader.readLine()) != null) {
                        log.error("script output:{}", error);
                    }
                }
            } catch (IOException | InterruptedException e) {
                log.error(e.getMessage());
                scriptResp.setCode(-1);
            }
            return scriptResp;
        });

        futures.put(generateScheduleKey(scriptResp.getScript(), scriptResp.getArgs()), future);
        return future;
    }


    public void stopScript(String scheduleKey, boolean mayInterruptIfRunning) {
        if (this.futures.containsKey(scheduleKey)) {
            futures.get(scheduleKey).cancel(mayInterruptIfRunning);
            this.futures.remove(scheduleKey);
        }
    }

    public String generateScheduleKey(String script, String... args) {
        return script + ":" + Arrays.toString(args);
    }


    public void stopAll() {
        if (!this.futures.isEmpty()) {
            for (Future<ScriptResp> future : this.futures.values()) {
                future.cancel(true);
            }
        }
    }

    public Set<String> getRunningScripts() {
        Set<String> runningScripts = new LinkedHashSet<>();
        if (MapUtil.isNotEmpty(this.futures)) {
            return this.futures.keySet();
        }

        return runningScripts;
    }

    public Future<ScriptResp> getRunningScriptFuture(String scheduleKey) {
        if (this.futures.containsKey(scheduleKey)) {
            return this.futures.get(scheduleKey);
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
//        System.out.println(Arrays.toString(new String[]{"1", "2", "3"}));
//        System.exit(0);
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        threadPoolTaskExecutor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        threadPoolTaskExecutor.setQueueCapacity(10000);
        threadPoolTaskExecutor.initialize();
        ScriptRunner scriptRunner = new ScriptRunner(threadPoolTaskExecutor);
//        for (int i = 0; i < 2; i++) {

        Future<ScriptResp> future = scriptRunner.runScript("D:\\workspaces\\java\\jllama\\scripts\\download.py", SCRIPT_TYPE.PYTHON, "--model", "unsloth/DeepSeek-R1-Distill-Qwen-1.5B-GGUF", "--file_pattern", "DeepSeek-R1-Distill-Qwen-1.5B-Q4_K_M.gguf");
//        Future<ScriptResp> future = scriptRunner.runScript("D:\\workspaces\\java\\jllama\\scripts\\test.bat", SCRIPT_TYPE.BAT, "--model", "unsloth/DeepSeek-R1-Distill-Qwen-1.5B-GGUF", "--file_pattern", "DeepSeek-R1-Distill-Qwen-1.5B-Q4_K_M.gguf");
//        TimeUnit.SECONDS.sleep(3);

//        future.cancel(true);
        ScriptResp scriptResp = future.get();
        System.out.println(scriptResp);

//        }
//        TimeUnit.SECONDS.sleep(10);
//        scriptRunner.stopScript(scriptRunner.generateScheduleKey("D:\\workspaces\\java\\jllama\\scripts\\test.bat", "1", "2", "3"), true);
//        if (!future.isCancelled()) {
//            ScriptResp scriptResp = future.get();
//            System.out.println(scriptResp);
//        }
//        threadPoolTaskExecutor.shutdown();
    }
}
