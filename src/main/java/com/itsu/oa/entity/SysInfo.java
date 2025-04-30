package com.itsu.oa.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_info")
public class SysInfo extends BaseEntity {

    private String platform;

    private String osArch;

    private String gpuPlatform;

    private String cppVersion;

}
