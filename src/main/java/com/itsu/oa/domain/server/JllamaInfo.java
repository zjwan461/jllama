package com.itsu.oa.domain.server;

import lombok.Data;

/**
 * @author jerry.su
 * @date 2025/5/1 13:55
 */
@Data
public class JllamaInfo {

    private String platform;

    private String osArch;

    private String gpuPlatform;

    private String cppVersion;

    private String factoryVersion;

    private String selfVersion;
}
