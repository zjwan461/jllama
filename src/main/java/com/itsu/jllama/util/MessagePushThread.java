package com.itsu.jllama.util;

import cn.hutool.json.JSONUtil;
import com.itsu.jllama.core.component.MessageQueue;
import com.itsu.jllama.core.component.Msg;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.FluxSink;

@Slf4j
public class MessagePushThread extends Thread {

    private final MessageQueue messageQueue;
    
    private final FluxSink<String> fluxSink;

    public MessagePushThread(String name, MessageQueue messageQueue, FluxSink<String> fluxSink) {
        super(name);
        this.messageQueue = messageQueue;
        this.fluxSink = fluxSink;
    }

    @Override
    public void run() {
        try {
            while (currentThread().isAlive()) {
                Msg msg = messageQueue.take();
                fluxSink.next(JSONUtil.toJsonStr(msg));
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            fluxSink.error(e);
        } finally {
            fluxSink.complete();
        }
    }
}
