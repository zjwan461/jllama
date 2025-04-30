package com.itsu.oa.controller.resp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckCppUpdateResp {

    private boolean update;

    private String cppVersion;

    private String updateUrl;

}
