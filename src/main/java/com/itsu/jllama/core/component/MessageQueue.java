package com.itsu.jllama.core.component;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author jerry.su
 * @date 2025/4/13 12:44
 */
@Slf4j
public class MessageQueue extends ArrayBlockingQueue<Msg> {

    private static final int capacity = 100;

    public MessageQueue() {
        super(capacity);
    }

    public MessageQueue(int capacity) {
        super(capacity);
    }

    public void put(Msg msg) throws InterruptedException {
        log.debug("put msg:{} start", msg);
        super.put(msg);
        log.debug("put msg:{} success", msg);
    }

    public Msg take() throws InterruptedException {
        log.debug("take msg start");
        Msg msg = super.take();
        log.debug("take msg success:{}", msg);
        return msg;
    }
}
