package com.itsu.oa.service;

import com.itsu.oa.controller.resp.CheckUpdateResp;

public interface CheckUpdateService {

    CheckUpdateResp checkCppUpdate();

    CheckUpdateResp checkFactoryUpdate();

    CheckUpdateResp checkSelfUpdate();
}
