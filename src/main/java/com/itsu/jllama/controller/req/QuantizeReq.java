package com.itsu.jllama.controller.req;

import lombok.Data;

@Data
public class QuantizeReq {

    private String originModel;

    private String outputModel;

    private String quantizeParam;

    private boolean async;

}
