package com.itsu.jllama.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("quantize")
public class Quantize extends BaseEntity {

    @TableField("`INPUT`")
    private String input;

    @TableField("`OUTPUT`")
    private String output;

    private String param;

    private boolean async;
}
