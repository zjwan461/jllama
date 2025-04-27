package com.itsu.oa.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("model")
@Data
public class Model extends BaseEntity {

    private String repo;

    private String name;

    private String downloadPlatform;

    private String files;

    private String saveDir;

    @TableField(exist = false)
    private String importDir;
}
