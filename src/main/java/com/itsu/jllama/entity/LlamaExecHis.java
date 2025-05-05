package com.itsu.jllama.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("llama_exec_his")
public class LlamaExecHis extends BaseEntity {

    private Long modelId;
    private String modelName;
    private Long fileId;
    private String filePath;
    private String fileName;
    private String llamaCppDir;
    private String llamaCppCommand;
    private String llamaCppArgs;
    private String logFilePath;
    private int status;
    private String pid;
}
