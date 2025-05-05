package com.itsu.jllama.schedule;

import com.itsu.jllama.controller.resp.CheckUpdateResp;
import com.itsu.jllama.core.component.MessageQueue;
import com.itsu.jllama.core.component.Msg;
import com.itsu.jllama.service.CheckUpdateService;
import com.itsu.jllama.service.SettingsService;
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

    @Resource
    private SettingsService settingsService;

    @Scheduled(cron = "0 0/30 * * * ?")
    public void checkLlamaCppUpdate() throws Exception {
        if (settingsService.getCachedSettings().isUpdatePush()) {
            CheckUpdateResp checkUpdateResp = checkUpdateService.checkCppUpdate();
            if (checkUpdateResp.isUpdate()) {
                String cppVersion = checkUpdateResp.getVersion();
                String updateUrl = checkUpdateResp.getUpdateUrl();
                Msg msg = new Msg("llama.cpp更新提醒", "存在新的软件版本" + cppVersion + ",访问" + updateUrl + "进行下载", LocalDateTime.now(), Msg.Status.info);
                messageQueue.put(msg);
            }
        }
    }
}
