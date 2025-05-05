package com.itsu.jllama.core.event.executor;

import com.itsu.jllama.core.event.LlammaShutdown;
import com.itsu.jllama.util.LlamaCppRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author jerry.su
 * @date 2025/4/5 12:10
 */
@Slf4j
@Component
public class LlammaShutdownListener {

    final LlamaCppRunner llamaCppRunner;

    public LlammaShutdownListener(LlamaCppRunner llamaCppRunner) {
        this.llamaCppRunner = llamaCppRunner;
    }

    @EventListener(LlammaShutdown.class)
    public void shutdown(LlammaShutdown llammaShutdown) {
        String source = (String) llammaShutdown.getSource();
        log.info("LlammaShutdownListener.shutdown get a signal: {}", source);
        if ("all".equals(source) ){
            llamaCppRunner.stopAll();
        } else {
            llamaCppRunner.stop(source, true);
        }
    }
}
