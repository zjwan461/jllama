package com.itsu.oa.core.sys;

import com.itsu.oa.util.ScriptRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
@Order
public class DestroyHook implements DisposableBean {

    @Resource
    private ScriptRunner scriptRunner;

    @Override
    public void destroy() throws Exception {
        log.info("start to shutdown jllama...");
        scriptRunner.stopAll();
    }
}
