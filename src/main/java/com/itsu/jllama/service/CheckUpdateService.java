package com.itsu.jllama.service;

import com.itsu.jllama.controller.resp.CheckUpdateResp;

public interface CheckUpdateService {

    CheckUpdateResp checkCppUpdate();

    CheckUpdateResp checkFactoryUpdate();

    CheckUpdateResp checkSelfUpdate();
}
