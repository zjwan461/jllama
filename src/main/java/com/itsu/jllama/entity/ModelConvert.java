package com.itsu.jllama.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("model_convert")
public class ModelConvert extends BaseEntity {

    @TableField("`INPUT`")
    private String input;
    @TableField("`OUTPUT`")
    private String output;
    private String scriptFile;
    private boolean async;
}
