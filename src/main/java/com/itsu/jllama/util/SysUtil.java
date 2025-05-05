package com.itsu.oa.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@Component
public class SysUtil {

    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    public void openBrowser(String url, String... command) {
        threadPoolTaskExecutor.submit(() -> {
            try {
                ArrayList<String> cmdList = CollUtil.toList(command);
                cmdList.add(url);
                ProcessBuilder processBuilder = new ProcessBuilder(ArrayUtil.toArray(cmdList, String.class));
                Process process = processBuilder.start();
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    log.info("打开浏览器时出现错误，退出码: {}", exitCode);
                }
            } catch (IOException | InterruptedException e) {
                log.error(e.getMessage(), e);
            }

        });

    }
}
