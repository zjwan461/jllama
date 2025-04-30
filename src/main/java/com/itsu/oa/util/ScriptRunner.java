package com.itsu.oa.util;

import cn.hutool.core.map.MapUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.InputStream;
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


    @Data
    public static class ScriptResp {
        private int code;
        private String script;
        private String scriptDir;
        private String[] args;
        private Process process;
    }


    public Future<ScriptResp> runScript(String scriptDir, String script, String... args) {
        String scheduleKey = generateScheduleKey(script, args);
        if (this.futures.containsKey(scheduleKey)) {
            log.info("当前脚本：{}运行中", scheduleKey);
            return this.futures.get(scheduleKey);
        }
        ScriptResp scriptResp = new ScriptResp();
        scriptResp.setScript(script);
        scriptResp.setScriptDir(scriptDir);
        scriptResp.setArgs(args);
        Future<ScriptResp> future = threadPool.submit(() -> {
            try {
                List<String> commandList = new ArrayList<>();
                commandList.add(scriptDir);
                commandList.add(script);
                if (args != null && args.length > 0) {
                    commandList.addAll(Arrays.asList(args));
                }
                // 创建 ProcessBuilder 对象，指定要执行的命令
                ProcessBuilder processBuilder = new ProcessBuilder(commandList);
                // 启动进程
                Process process = processBuilder.start();
                scriptResp.setProcess(process);
//                // 获取脚本执行的输出流
                logScript(process, script);

            } catch (Exception e) {
                log.error(e.getMessage(), e);
                scriptResp.setCode(-1);
            }
            return scriptResp;
        });

        futures.put(generateScheduleKey(scriptResp.getScript(), scriptResp.getArgs()), future);
        return future;
    }

    private void logScript(Process process,String script) {
        InputStream is = process.getInputStream();
        InputStream es = process.getErrorStream();
        ScriptLogThread infoThread = new ScriptLogThread(is, script, "script-info");
        infoThread.start();
        ScriptLogThread errThread = new ScriptLogThread(es, script, "script-err");
        errThread.start();
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

//        Future<ScriptResp> future = scriptRunner.runScript("D:\\workspaces\\java\\jllama\\scripts\\download.py", SCRIPT_TYPE.PYTHON, "--model", "unsloth/DeepSeek-R1-Distill-Qwen-1.5B-GGUF", "--file_pattern", "DeepSeek-R1-Distill-Qwen-1.5B-Q4_K_M.gguf");
        Future<ScriptResp> future = scriptRunner.runScript("C:\\Users\\1\\.conda\\envs\\llamafactory\\python",System.getProperty("user.dir") + "/scripts/convert_hf_to_gguf.py", "D:\\models\\Qwen\\Qwen3-1___7B","--outfile","D:\\models/1.gguf");
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
