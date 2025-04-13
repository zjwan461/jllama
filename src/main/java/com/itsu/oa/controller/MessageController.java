package com.itsu.oa.controller;

import cn.hutool.json.JSONUtil;
import com.itsu.oa.core.component.MessageQueue;
import com.itsu.oa.core.component.Msg;
import com.itsu.oa.core.model.R;
import com.itsu.oa.core.mvc.Auth;
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
@RestController
public class MessageController {

    @Resource
    HttpServletRequest request;

    @Resource
    MessageQueue messageQueue;

    @Resource
    ThreadPoolTaskExecutor threadPool;

    @Auth
    @GetMapping("/api/message")
    public Flux<String> message() {
        return Flux.create(fluxSink -> {
            threadPool.submit(() -> {
                while (true) {
                    try {
                        Msg msg = messageQueue.take();
                        fluxSink.next(JSONUtil.toJsonStr(msg));
                    } catch (Exception e) {
                        fluxSink.error(e);
                        break;
                    }
                }
                fluxSink.complete();
            });
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
