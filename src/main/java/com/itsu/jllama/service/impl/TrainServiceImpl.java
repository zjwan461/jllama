package com.itsu.jllama.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.itsu.jllama.controller.req.TrainReq;
import com.itsu.jllama.core.sys.Platform;
import com.itsu.jllama.entity.Settings;
import com.itsu.jllama.entity.SysInfo;
import com.itsu.jllama.service.SettingsService;
import com.itsu.jllama.service.TrainService;
import com.itsu.jllama.util.GradioUtil;
import com.itsu.jllama.util.ScriptRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.Future;

@Slf4j
@Service
public class TrainServiceImpl implements TrainService {

    public static final String STOP_STATUS = "STOP";
    public static final String STARTING_STATUS = "STARTING";
    public static final String STARTED_STATUS = "STARTED";

    public static final String LLAMA_FACTORY_WEBUI_PROCESS_ID = "LLAMA_FACTORY_WEBUI_PROCESS";

    private static final Map<String, Object> pingData = JSONUtil.parseObj("{\"data\":[\"Aya-23-8B-Chat\"]}");

    @Resource
    private ScriptRunner scriptRunner;

    @Resource
    private SettingsService settingsService;

    @Override
    public void startLlamaFactoryWebUi() {
        Settings settings = settingsService.getCachedSettings();
        SysInfo sysInfo = SpringUtil.getBean(SysInfo.class);
        Platform platform = Platform.match(sysInfo.getPlatform());
        String scriptPath = settings.getPyDir();
        if (platform == Platform.WINDOWS) {
            scriptPath = scriptPath + "/Scripts/llamafactory-cli";
        } else {
            scriptPath = scriptPath + "/llamafactory-cli";
        }
        Integer factoryPort = settings.getFactoryPort();
        if (factoryPort != null) {
            scriptRunner.runLlamaFactory(LLAMA_FACTORY_WEBUI_PROCESS_ID, scriptPath, "webui", "GRADIO_SERVER_PORT=" + factoryPort);
        } else {
            scriptRunner.runLlamaFactory(LLAMA_FACTORY_WEBUI_PROCESS_ID, scriptPath, "webui");
        }
    }

    @Override
    public String getPreviewCommand(TrainReq trainReq) {
        Map<String, Object> map = buildGradioTrainData(trainReq);
        String urlPrefix = this.getLlamaFactoryWebUiUrl();
        String result = GradioUtil.api(urlPrefix + "/gradio_api/call/preview_train", map);
        JSONArray array = JSONUtil.parseArray(result);
        if (!array.isEmpty()) {
            return (String) array.get(0);
        }
        return "";
    }

    @Override
    public boolean isLlamaFactoryWebUiRunning() {
        return STARTED_STATUS.equals(this.getLlamaFactoryWebUiStatus());
    }

    @Override
    public Flux<String> runTrain(TrainReq trainReq) {
        Map<String, Object> map = buildGradioTrainData(trainReq);
        return GradioUtil.apiWithFlux(this.getLlamaFactoryWebUiUrl() + "/gradio_api/call/run_train", map);
    }

    @Override
    public Map<String, Object> buildGradioTrainData(TrainReq trainReq) {
        String originJson = "{\n" +
                "  \"data\": [\n" +
                "    \"zh\",\n" +
                "    \"" + trainReq.getModelName() + "\",\n" +
                "    \"" + trainReq.getModelPath() + "\",\n" +
                "    \"lora\",\n" +
                "    [],\n" +
                "    \"none\",\n" +
                "    \"bitsandbytes\",\n" +
                "    \"default\",\n" +
                "    \"none\",\n" +
                "    \"auto\",\n" +
                "    \"Supervised Fine-Tuning\",\n" +
                "    \"" + trainReq.getDatasetPath() + "\",\n" +
                "    [\n" +
                "      \"" + trainReq.getDataset() + "\"\n" +
                "    ],\n" +
                "    \"5e-5\",\n" +
                "    \"" + trainReq.getTrainTimes() + "\",\n" +
                "    \"1.0\",\n" +
                "    \"100000\",\n" +
                "    \"bf16\",\n" +
                "    " + trainReq.getCutLen() + ",\n" +
                "    2,\n" +
                "    8,\n" +
                "    0,\n" +
                "    \"cosine\",\n" +
                "    5,\n" +
                "    100,\n" +
                "    0,\n" +
                "    0,\n" +
                "    \"{\\\"optim\\\": \\\"adamw_torch\\\"}\",\n" +
                "    false,\n" +
                "    false,\n" +
                "    false,\n" +
                "    false,\n" +
                "    false,\n" +
                "    false,\n" +
                "    [\n" +
                "      \"none\"\n" +
                "    ],\n" +
                "    2,\n" +
                "    \"all\",\n" +
                "    \"\",\n" +
                "    8,\n" +
                "    16,\n" +
                "    0,\n" +
                "    0,\n" +
                "    false,\n" +
                "    false,\n" +
                "    false,\n" +
                "    false,\n" +
                "    \"\",\n" +
                "    \"\",\n" +
                "    0.1,\n" +
                "    0,\n" +
                "    \"sigmoid\",\n" +
                "    [],\n" +
                "    false,\n" +
                "    false,\n" +
                "    false,\n" +
                "    16,\n" +
                "    200,\n" +
                "    2,\n" +
                "    \"all\",\n" +
                "    false,\n" +
                "    16,\n" +
                "    200,\n" +
                "    32,\n" +
                "    \"all\",\n" +
                "    false,\n" +
                "    \"layer\",\n" +
                "    \"ascending\",\n" +
                "    50,\n" +
                "    0.05,\n" +
                "    false,\n" +
                "    \"llamafactory\",\n" +
                "    \"\",\n" +
                "    \"\",\n" +
                "    \"\",\n" +
                "    \"cloud\",\n" +
                "    \"" + trainReq.getOutputDir() + "\",\n" +
                "    \"" + trainReq.getOutputConfigPath() + "\",\n" +
                "    1,\n" +
                "    \"none\",\n" +
                "    false\n" +
                "  ]\n" +
                "}";
        return JSONUtil.parseObj(originJson);
    }

    @Override
    public String getLlamaFactoryWebUiStatus() {
        Future<ScriptRunner.ScriptResp> future = scriptRunner.getRunningScriptFuture(LLAMA_FACTORY_WEBUI_PROCESS_ID);
        if (future != null && !future.isCancelled()) {
            String resp = null;
            try {
                resp = GradioUtil.api(this.getLlamaFactoryWebUiUrl() + "/gradio_api/call/get_model_info", pingData);
            } catch (Exception e) {
                log.info("request gradio fail, may not started finished", e);
            }
            return StrUtil.isNotBlank(resp) ? STARTED_STATUS : STARTING_STATUS;
        }
        return STOP_STATUS;
    }

    @Override
    public String getLlamaFactoryWebUiUrl() {
        Settings settings = settingsService.getCachedSettings();
        return "http://127.0.0.1:" + settings.getFactoryPort();
    }
}
