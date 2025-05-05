package com.itsu.jllama.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ProcessLogThread extends Thread {

    private static final Logger log = LoggerFactory.getLogger("processLog");

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
