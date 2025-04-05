package com.itsu.oa.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author jerry.su
 * @date 2025/4/5 15:44
 */
@TableName("settings")
@Data
public class Settings {

    private String llamaCppDir;
    private String modelSaveDir;
    private String logSaveDir;
    private int logLine;
    private int logSaveDay;
}
