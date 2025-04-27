package com.itsu.oa.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Slf4j
public class ProcessLogThread extends Thread {

    private final InputStream is;

    private final LlamaCppRunner.LlamaCommand command;

    public ProcessLogThread(InputStream is, LlamaCppRunner.LlamaCommand command, String name) {
        super(name);
        this.is = is;
        this.command = command;
    }

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line = "";
            while ((line = br.readLine()) != null) {
                log.info("{}:{}", command.getCommand(), line);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
