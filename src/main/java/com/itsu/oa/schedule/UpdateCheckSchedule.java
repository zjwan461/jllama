package com.itsu.oa.schedule;

import com.itsu.oa.controller.resp.CheckCppUpdateResp;
import com.itsu.oa.core.component.MessageQueue;
import com.itsu.oa.core.component.Msg;
import com.itsu.oa.service.CheckUpdateService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Component
public class UpdateCheckSchedule {

    @Resource
    private MessageQueue messageQueue;

    @Resource
    private CheckUpdateService checkUpdateService;

    @Scheduled(cron = "0 0/30 * * * ?")
    public void checkLlamaCppUpdate() throws Exception {
        CheckCppUpdateResp checkCppUpdateResp = checkUpdateService.checkCppUpdate();
        if (checkCppUpdateResp.isUpdate()) {
            String cppVersion = checkCppUpdateResp.getCppVersion();
            String updateUrl = checkCppUpdateResp.getUpdateUrl();
            Msg msg = new Msg("llama.cpp更新提醒", "存在新的软件版本" + cppVersion + ",访问" + updateUrl + "进行下载", LocalDateTime.now(), Msg.Status.info);
            messageQueue.put(msg);
        }
    }
}
