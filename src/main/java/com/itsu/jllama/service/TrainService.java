package com.itsu.jllama.service;

import com.itsu.jllama.controller.req.TrainReq;
import reactor.core.publisher.Flux;

import java.util.Map;

public interface TrainService {

    void startLlamaFactoryWebUi();

    String getPreviewCommand(TrainReq trainReq);

    boolean isLlamaFactoryWebUiRunning();

    Flux<String> runTrain(TrainReq trainReq);

    Map<String, Object> buildGradioTrainData(TrainReq trainReq);
}
