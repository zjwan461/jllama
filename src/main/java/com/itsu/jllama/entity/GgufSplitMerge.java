package com.itsu.jllama.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author jerry.su
 * @date 2025/4/20 15:29
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("gguf_split_merge")
public class GgufSplitMerge extends BaseEntity {

    private String option;

    @TableField("`INPUT`")
    private String input;

    @TableField("`OUTPUT`")
    private String output;

    private String splitOption;

    private String splitParam;

    private boolean async;

}
