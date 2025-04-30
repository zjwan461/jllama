package com.itsu.oa.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.itsu.oa.config.JllamaConfigProperties;
import com.itsu.oa.controller.resp.CheckUpdateResp;
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
    public CheckUpdateResp checkCppUpdate() {
        JllamaConfigProperties.CheckUpdate checkUpdate = jllamaConfigProperties.getCheckUpdate();
        String checkUrl = checkUpdate.getCppUrl();
        String xpath = checkUpdate.getCppXpath();
        CheckUpdateResp.CheckUpdateRespBuilder builder = CheckUpdateResp.builder();
        builder.update(false);
        SysInfo sysInfo = sysInfoMapper.selectOne(Wrappers.lambdaQuery(SysInfo.class)
                .last("limit 1"));
        if (Objects.nonNull(sysInfo)) {
            String cppVersion = sysInfo.getCppVersion();
            String newVersion = JsoupUtil.getValue(checkUrl, xpath);
            if (checkPureNumVersion(cppVersion, newVersion)) {
                builder.update(true)
                        .version(newVersion)
                        .updateUrl(checkUpdate.getCppUrl());
            } else {
                builder.update(false)
                        .version(cppVersion);
            }
        }
        return builder.build();
    }

    @Override
    public CheckUpdateResp checkFactoryUpdate() {
        JllamaConfigProperties.CheckUpdate checkUpdate = jllamaConfigProperties.getCheckUpdate();
        String checkUrl = checkUpdate.getFactoryUrl();
        String xpath = checkUpdate.getFactoryXpath();

        CheckUpdateResp.CheckUpdateRespBuilder builder = CheckUpdateResp.builder();
        builder.update(false);
        SysInfo sysInfo = sysInfoMapper.selectOne(Wrappers.lambdaQuery(SysInfo.class)
                .last("limit 1"));
        if (Objects.nonNull(sysInfo)) {
            String cppVersion = sysInfo.getFactoryVersion();
            String newVersion = JsoupUtil.getValue(checkUrl, xpath);
            if (checkFloatNumVersion(cppVersion, newVersion)) {
                builder.update(true)
                        .version(newVersion)
                        .updateUrl(checkUpdate.getCppUrl());
            } else {
                builder.update(false)
                        .version(cppVersion);
            }
        }
        return builder.build();
    }

    @Override
    public CheckUpdateResp checkSelfUpdate() {
        JllamaConfigProperties.CheckUpdate checkUpdate = jllamaConfigProperties.getCheckUpdate();
        String checkUrl = checkUpdate.getSelfUrl();
        String xpath = checkUpdate.getSelfXpath();

        CheckUpdateResp.CheckUpdateRespBuilder builder = CheckUpdateResp.builder();
        builder.update(false);
        SysInfo sysInfo = sysInfoMapper.selectOne(Wrappers.lambdaQuery(SysInfo.class)
                .last("limit 1"));
        if (Objects.nonNull(sysInfo)) {
            String cppVersion = sysInfo.getSelfVersion();
            String newVersion = JsoupUtil.getValue(checkUrl, xpath);
            if (checkFloatNumVersion(cppVersion, newVersion)) {
                builder.update(true)
                        .version(newVersion)
                        .updateUrl(checkUpdate.getCppUrl());
            } else {
                builder.update(false)
                        .version(cppVersion);
            }
        }
        return builder.build();
    }

    private boolean checkPureNumVersion(String cppVersion, String newVersion) {
        try {
            int versionNum = Integer.parseInt(cppVersion.trim().substring(1));
            int newVersionNum = Integer.parseInt(newVersion.trim().substring(1));
            return newVersionNum > versionNum;
        } catch (Exception e) {
            log.error("check cpp update fail", e);
            return false;
        }
    }

    private boolean checkFloatNumVersion(String cppVersion, String newVersion) {
        try {
            int versionNum = Integer.parseInt(cppVersion.trim().substring(1).replace(".", ""));
            int newVersionNum = Integer.parseInt(newVersion.trim().substring(1).replace(".", ""));
            return newVersionNum > versionNum;
        } catch (Exception e) {
            log.error("check cpp update fail", e);
            return false;
        }
    }

}
