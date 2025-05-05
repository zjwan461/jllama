package com.itsu.jllama.controller;

import com.itsu.jllama.core.component.MessageQueue;
import com.itsu.jllama.core.component.Msg;
import com.itsu.jllama.core.model.R;
import com.itsu.jllama.core.mvc.Auth;
import com.itsu.jllama.util.MessagePushThread;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * @author jerry.su
 * @date 2025/4/13 13:03
 */
@Slf4j
@RestController
public class MessageController {

    @Resource
    HttpServletRequest request;

    @Resource
    MessageQueue messageQueue;

    @Resource
    ThreadPoolTaskExecutor threadPool;

    MessagePushThread messagePushThread = null;

    @Auth
    @GetMapping("/api/message")
    public Flux<String> message() {
        return Flux.create(fluxSink -> {
            if (messagePushThread == null) {
                log.info("message push thread is not prepared");
                messagePushThread = new MessagePushThread("msgThread-" + System.currentTimeMillis(), messageQueue, fluxSink);
            } else if (!messagePushThread.isAlive() || messagePushThread.isInterrupted()) {
                log.info("message push thread is not alive or is interrupted");
            } else {
                log.info("message push thread is active and running, will be interrupt and restart");
                messagePushThread.interrupt();
                messagePushThread = new MessagePushThread("msgThread-" + System.currentTimeMillis(), messageQueue, fluxSink);
            }
            messagePushThread.start();
        });
    }

    @Auth
    @GetMapping("/api/message/test")
    public R test() throws InterruptedException {
        Msg msg = new Msg();
        msg.setTitle("title");
        msg.setContent("content");
        msg.setStatus(Msg.Status.success);
        msg.setCreateTime(LocalDateTime.now());
        messageQueue.put(msg);
        return R.success();
    }
}
