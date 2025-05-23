package com.itsu.jllama.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "jllama")
public class JllamaConfigProperties {

    private Auth auth;

    private Model model;

    private String llamaCpuDir;

    private String llamaLogDir;

    private Gpu gpu;

    private Quantize quantize;

    private CheckUpdate checkUpdate;

    @Data
    public static class Quantize {
        private List<String> supportedTypes;
    }


    @Data
    public static class Auth {
        private String ignorePaths;
    }

    @Data
    public static class Model {
        private String saveDir;
        private String primaryStage = "modelScope";
        private String modelScopeFileListUriPrefix = "https://modelscope.cn/api/v1/models";
        private String modelScopeFileDownloadUriPrefix = "https://modelscope.cn/models";
    }

    @Data
    public static class Gpu {
        private boolean enable;
        private String llamaDir;
    }

    @Data
    public static class CheckUpdate {
        private String cppUrl;
        private String cppXpath;
        private String factoryUrl;
        private String factoryXpath;
        private String selfUrl;
        private String selfXpath;
    }
}
