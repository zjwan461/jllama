package com.itsu.jllama.controller.req;

import lombok.Data;

/**
 * @author jerry.su
 * @date 2025/4/20 13:39
 */
@Data
public class SplitMergeReq {
    private String options;
    private String input;
    private String output;
    private String splitOption;
    private String splitParam;
    private boolean async;
}
