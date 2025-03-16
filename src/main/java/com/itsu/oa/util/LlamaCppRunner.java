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
public class LlamaCppRunner {

    private final ThreadPoolTaskExecutor threadPool;

    private LinkedHashMap<String, Future> futures = new LinkedHashMap<>();

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
    }






    public void stop(String scheduleKey, boolean mayInterruptIfRunning) {
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
            for (Future future : this.futures.values()) {
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

    public Future getRunningServicetFuture(String scheduleKey) {
        if (this.futures.containsKey(scheduleKey)) {
            return this.futures.get(scheduleKey);
        }
        return null;
    }

}
