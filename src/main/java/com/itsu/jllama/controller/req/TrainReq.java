package com.itsu.jllama.controller.req;

import lombok.Data;

@Data
public class TrainReq {
    private String modelName;
    private String modelPath;
    private String datasetPath;
    private String dataset;
    private int trainTimes;
    private int cutLen;
    private String outputDir;
    private String outputConfigPath;
    private boolean logOutput;
}
