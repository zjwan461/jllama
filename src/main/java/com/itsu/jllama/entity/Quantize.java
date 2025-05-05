package com.itsu.jllama.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("quantize")
public class Quantize extends BaseEntity {

    private String input;

    private String output;

    private String param;

    private boolean async;
}
