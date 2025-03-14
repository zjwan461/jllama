package com.itsu.oa.controller.req;

import lombok.Data;

@Data
public class FileDownloadReq {

    private Long modelId;

    private String modelName;

    private Long fileSize;

    private String fileName;
}
