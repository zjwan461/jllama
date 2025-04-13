package com.itsu.oa.service.aop;

import com.itsu.oa.core.component.MessageQueue;
import com.itsu.oa.core.component.Msg;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Component
@Aspect
public class MessageAspect {

    @Resource
    private MessageQueue messageQueue;


    @Pointcut(value = "@annotation(com.itsu.oa.service.aop.Message)")
    public void rule() {
    }


    @Around(value = "rule()&&@annotation(message)")
    public Object handle(ProceedingJoinPoint joinPoint, Message message) throws Throwable {
        Msg msg = new Msg();
        msg.setTitle(message.title());
        Object result = null;
        try {
            result = joinPoint.proceed();
            msg.setContent(message.success());
            msg.setStatus(Msg.Status.success);
        } catch (Throwable e) {
            msg.setContent(message.fail());
            msg.setStatus(Msg.Status.error);
            throw e;
        } finally {
            msg.setCreateTime(LocalDateTime.now());
            messageQueue.put(msg);
        }
        return result;
    }

}
