package com.itsu.oa.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jllama")
public class JllamaConfigProperties {

    private Auth auth;

    private Model model;

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
}
