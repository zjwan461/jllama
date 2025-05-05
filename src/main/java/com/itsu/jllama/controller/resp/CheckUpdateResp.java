package com.itsu.jllama.controller.resp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckUpdateResp {

    private boolean update;

    private String version;

    private String updateUrl;

}
