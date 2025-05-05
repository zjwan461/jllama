package com.itsu.oa.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("MODEL_CONVERT")
public class ModelConvert extends BaseEntity {

    private String input;
    private String output;
    private String scriptFile;
    private boolean async;
}
