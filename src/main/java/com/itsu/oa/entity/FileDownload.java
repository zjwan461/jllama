package com.itsu.oa.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FileDownload extends BaseEntity {

    private Long modelId;
    private String modelName;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String type;
    @TableField(exist = false)
    private String percent = "0%";
}
