package com.itsu.jllama.controller.req;

import com.itsu.jllama.util.LlamaCppRunner;
import lombok.Data;

@Data
public class NewProcessReq {

    // Model configuration parameters
    private Long modelId;
    private Long fileId;
    private String host;
    private int port;
    private LlamaCppRunner.LlamaCommand llamaCommand;
    private int ngl;
    private int threads;
    private int ctxSize;
    private int parallel;
    private String args;
}
