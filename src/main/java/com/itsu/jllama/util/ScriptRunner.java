package com.itsu.jllama.util;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import com.itsu.jllama.core.component.MessageQueue;
import com.itsu.jllama.core.component.Msg;
import com.itsu.jllama.core.exception.JException;
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

    private final MessageQueue messageQueue;

    private final LinkedHashMap<String, Future<ScriptResp>> futures = new LinkedHashMap<>();

    public ScriptRunner(ThreadPoolTaskExecutor threadPool, MessageQueue messageQueue) {
        this.threadPool = threadPool;
        this.messageQueue = messageQueue;
    }


    @Data
    public static class ScriptResp {
        private int code;
        private String script;
        private String scriptDir;
        private String[] args;
        private Process process;
    }

    @Data
    public static class ScriptOutputResp {
        private boolean success;
        private String infoOutput;
        private String errOutput;
    }

    public ScriptOutputResp runScriptAndRead(String scriptDir, String script, boolean infoStream, boolean errStream, String... args) {
        ScriptOutputResp resp = new ScriptOutputResp();
        List<String> commandList = new ArrayList<>();
        commandList.add(scriptDir);
        commandList.add(script);
        if (args != null && args.length > 0) {
            commandList.addAll(Arrays.asList(args));
        }
        ProcessBuilder processBuilder = new ProcessBuilder(commandList);
        // 启动进程
        Process process = null;
        try {
            process = processBuilder.start();
            String infoStr = "";
            if (infoStream) {
                InputStream inputStream = process.getInputStream();
                infoStr = IoUtil.readUtf8(inputStream);
            }
            String errStr = "";
            if (errStream) {
                InputStream errorStream = process.getErrorStream();
                errStr = IoUtil.readUtf8(errorStream);
            }
            int result = process.waitFor();
            resp.setSuccess(result == 0);
            resp.setInfoOutput(infoStr);
            resp.setErrOutput(errStr);
            return resp;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new JException("script 执行失败");
        }
    }

    public Future<ScriptResp> runLlamaFactory(String processId, String scriptDir, String script, String... env) {
        ScriptResp scriptResp = new ScriptResp();
        scriptResp.setScript(script);
        scriptResp.setScriptDir(scriptDir);
        List<String> commandList = new ArrayList<>();
        commandList.add(scriptDir);
        commandList.add(script);
        Future<ScriptResp> future = threadPool.submit(() -> {
            try {
                // 创建 ProcessBuilder 对象，指定要执行的命令
                ProcessBuilder processBuilder = new ProcessBuilder(commandList);
                // 设置环境变量
                if (!Objects.isNull(env)) {
                    Map<String, String> environment = processBuilder.environment();
                    for (String item : env) {
                        String[] envParam = item.split("=");
                        if (envParam.length == 2) {
                            environment.put(envParam[0], envParam[1]);
                        }
                    }
                }
                // 启动进程
                Process process = processBuilder.start();
                scriptResp.setProcess(process);
                // 获取脚本执行的输出流
                logScript(process, script);

            } catch (Exception e) {
                log.error(e.getMessage(), e);
                scriptResp.setCode(-1);
            } finally {
                this.handleAsyncMsgPush("执行脚本", scriptResp.getProcess(), processId);
            }
            return scriptResp;
        });
        futures.put(processId, future);
        return future;
    }

    public String runScript(String scriptDir, String script, boolean async, String... args) {
        String scheduleKey = generateScheduleKey(script, args);
        ScriptResp scriptResp = new ScriptResp();
        scriptResp.setScript(script);
        scriptResp.setScriptDir(scriptDir);
        scriptResp.setArgs(args);
        List<String> commandList = new ArrayList<>();
        commandList.add(scriptDir);
        commandList.add(script);
        if (args != null && args.length > 0) {
            commandList.addAll(Arrays.asList(args));
        }
        if (async) {
            Future<ScriptResp> future = threadPool.submit(() -> {
                try {
                    // 创建 ProcessBuilder 对象，指定要执行的命令
                    ProcessBuilder processBuilder = new ProcessBuilder(commandList);
                    // 启动进程
                    Process process = processBuilder.start();
                    scriptResp.setProcess(process);
                    // 获取脚本执行的输出流
                    logScript(process, script);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    scriptResp.setCode(-1);
                } finally {
                    this.handleAsyncMsgPush("执行脚本", scriptResp.getProcess(), scheduleKey);
                }
                return scriptResp;
            });
            futures.put(scheduleKey, future);
        } else {
            ProcessBuilder processBuilder = new ProcessBuilder(commandList);
            // 启动进程
            Process process = null;
            try {
                process = processBuilder.start();
                scriptResp.setProcess(process);
                logScript(process, script);
                int result = process.waitFor();
                if (result != 0) {
                    log.error("script 执行失败,return code={}", result);
                    throw new JException("script 执行失败, return code=" + result);
                }
            } catch (JException e) {
                throw e;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new JException("script 执行失败");
            }
        }

        return scheduleKey;
    }

    private void logScript(Process process, String script) {
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
        return script + ":" + Arrays.toString(args) + ":" + IdUtil.fastSimpleUUID();
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

    public void handleAsyncMsgPush(String optionsDetail, Process process, String execId) {
        if (process != null) {
            Msg msg = new Msg();
            msg.setTitle(optionsDetail);

            try {
                process.waitFor();
                if (process.exitValue() == 0) {
                    msg.setContent(optionsDetail + "成功");
                    msg.setStatus(Msg.Status.success);
                    log.info("execID: {} script Exit Code: {}", execId, process.exitValue());
                } else {
                    msg.setContent(optionsDetail + "失败, exit code=" + process.exitValue());
                    msg.setStatus(Msg.Status.error);
                    log.error("execID: {} script Exit Code: {}", execId, process.exitValue());
                }
            } catch (InterruptedException e) {
                msg.setContent(optionsDetail + "失败");
                msg.setStatus(Msg.Status.error);
                Thread.currentThread().interrupt();
                log.error("execID: {} Thread interrupted", execId, e);
            } finally {
                try {
                    messageQueue.put(msg);
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                }
                this.stopScript(execId, true);
            }
        }
    }

    public static void main(String[] args) throws Exception {
//        System.out.println(Arrays.toString(new String[]{"1", "2", "3"}));
//        System.exit(0);
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        threadPoolTaskExecutor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        threadPoolTaskExecutor.setQueueCapacity(10000);
        threadPoolTaskExecutor.initialize();


        ScriptRunner scriptRunner = new ScriptRunner(threadPoolTaskExecutor, new MessageQueue());

        ScriptOutputResp resp = scriptRunner.runScriptAndRead("C:\\Users\\1\\.conda\\envs\\llamafactory\\Scripts\\llamafactory-cli", "version", true, false);
        System.out.println(resp);
        resp = scriptRunner.runScriptAndRead("D:\\Program Files\\llama-b5215-bin-win-avx-x64\\llama-cli", "--version", true, true);
        System.out.println(resp);
//        for (int i = 0; i < 2; i++) {

//        Future<ScriptResp> future = scriptRunner.runScript("D:\\workspaces\\java\\jllama\\scripts\\download.py", SCRIPT_TYPE.PYTHON, "--model", "unsloth/DeepSeek-R1-Distill-Qwen-1.5B-GGUF", "--file_pattern", "DeepSeek-R1-Distill-Qwen-1.5B-Q4_K_M.gguf");
//        Future<ScriptResp> future = scriptRunner.runScript("C:\\Users\\1\\.conda\\envs\\llamafactory\\python", System.getProperty("user.dir") + "/scripts/convert_hf_to_gguf.py", false, "D:\\models\\Qwen\\Qwen3-1.7B", "--outfile", "D:\\models/1.gguf");
//        Future<ScriptResp> future = scriptRunner.runScript("D:\\workspaces\\java\\jllama\\scripts\\test.bat", SCRIPT_TYPE.BAT, "--model", "unsloth/DeepSeek-R1-Distill-Qwen-1.5B-GGUF", "--file_pattern", "DeepSeek-R1-Distill-Qwen-1.5B-Q4_K_M.gguf");
//        TimeUnit.SECONDS.sleep(3);

//        future.cancel(true);
//        ScriptResp scriptResp = future.get();
//        System.out.println(scriptResp);

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
