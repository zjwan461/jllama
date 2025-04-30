package com.itsu.oa.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.itsu.oa.config.JllamaConfigProperties;
import com.itsu.oa.controller.resp.CheckCppUpdateResp;
import com.itsu.oa.entity.SysInfo;
import com.itsu.oa.mapper.SysInfoMapper;
import com.itsu.oa.service.CheckUpdateService;
import com.itsu.oa.util.JsoupUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Service
@Slf4j
public class CheckUpdateServiceImpl implements CheckUpdateService {

    @Resource
    private JllamaConfigProperties jllamaConfigProperties;

    @Resource
    private SysInfoMapper sysInfoMapper;

    @Override
    public CheckCppUpdateResp checkCppUpdate() {
        CheckCppUpdateResp.CheckCppUpdateRespBuilder builder = CheckCppUpdateResp.builder();
        builder.update(false);
        SysInfo sysInfo = sysInfoMapper.selectOne(Wrappers.lambdaQuery(SysInfo.class)
                .last("limit 1"));
        if (Objects.nonNull(sysInfo)) {
            String cppVersion = sysInfo.getCppVersion();
            JllamaConfigProperties.CheckUpdate checkUpdate = jllamaConfigProperties.getCheckUpdate();
            String newVersion = JsoupUtil.getValue(checkUpdate.getCppUrl(), checkUpdate.getCppXpath());
            if (checkCppUpdate(cppVersion, newVersion)) {
                builder.update(true)
                        .cppVersion(newVersion)
                        .updateUrl(checkUpdate.getCppUrl());
            } else {
                builder.update(false)
                        .cppVersion(cppVersion);
            }
        }
        return builder.build();
    }

    private boolean checkCppUpdate(String cppVersion, String newVersion) {
        try {
            int versionNum = Integer.parseInt(cppVersion.trim().substring(1));
            int newVersionNum = Integer.parseInt(newVersion.trim().substring(1));
            return newVersionNum > versionNum;
        } catch (Exception e) {
            log.error("check cpp update fail", e);
            return false;
        }
    }

}
