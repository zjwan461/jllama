package com.itsu.jllama.core.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author jerry.su
 * @date 2025/4/5 12:09
 */
public class LlammaShutdown extends ApplicationEvent {
    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    public LlammaShutdown(Object source) {
        super(source);
    }
}
