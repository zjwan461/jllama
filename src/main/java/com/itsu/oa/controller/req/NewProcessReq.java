package com.itsu.oa.controller.req;

import com.itsu.oa.util.LlamaCppRunner;
import lombok.Data;

@Data
public class NewProcessReq {

    private Long modelId;
    private Long fileId;
    private int port;
    private LlamaCppRunner.LlamaCommand llamaCommand;
    private int ngl;
    private int threads;
    private int ctxSize;
    private int parallel;
    private String args;
}
