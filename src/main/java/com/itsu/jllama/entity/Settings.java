package com.itsu.jllama.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author jerry.su
 * @date 2025/4/5 15:44
 */
@TableName("settings")
@Data
public class Settings {

    public static final String DEFAULT_ID = "0000";

    @TableId(type = IdType.INPUT)
    private String id;
    private String llamaCppDir;
    private String modelSaveDir;
    private String logSaveDir;
    private int logLine;
    private int logSaveDay;
    private boolean gpuFlag;
    private boolean updatePush;
    private String pyDir;
}
