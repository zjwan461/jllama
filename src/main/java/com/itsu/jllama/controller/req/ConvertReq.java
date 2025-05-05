package com.itsu.jllama.controller.req;

import lombok.Data;

@Data
public class ConvertReq {

    private String input;
    private String output;
    private String scriptFile;
    private boolean async;
}
