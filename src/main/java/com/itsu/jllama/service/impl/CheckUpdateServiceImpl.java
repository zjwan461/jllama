package com.itsu.jllama.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.itsu.jllama.config.JllamaConfigProperties;
import com.itsu.jllama.controller.resp.CheckUpdateResp;
import com.itsu.jllama.entity.SysInfo;
import com.itsu.jllama.mapper.SysInfoMapper;
import com.itsu.jllama.service.CheckUpdateService;
import com.itsu.jllama.service.SettingsService;
import com.itsu.jllama.util.JsoupUtil;
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

    @Resource
    private SettingsService settingsService;

    @Override
    public CheckUpdateResp checkCppUpdate() {
        String proxyIp = settingsService.getCachedSettings().getProxyIp();
        Integer proxyPort = settingsService.getCachedSettings().getProxyPort();
        JllamaConfigProperties.CheckUpdate checkUpdate = jllamaConfigProperties.getCheckUpdate();
        String checkUrl = checkUpdate.getCppUrl();
        String xpath = checkUpdate.getCppXpath();
        CheckUpdateResp.CheckUpdateRespBuilder builder = CheckUpdateResp.builder();
        builder.update(false);
        SysInfo sysInfo = sysInfoMapper.selectOne(Wrappers.lambdaQuery(SysInfo.class)
                .last("limit 1"));
        if (Objects.nonNull(sysInfo)) {
            String cppVersion = sysInfo.getCppVersion();
            String newVersion = JsoupUtil.getValue(checkUrl, xpath, proxyIp, proxyPort);
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
        String proxyIp = settingsService.getCachedSettings().getProxyIp();
        Integer proxyPort = settingsService.getCachedSettings().getProxyPort();
        JllamaConfigProperties.CheckUpdate checkUpdate = jllamaConfigProperties.getCheckUpdate();
        String checkUrl = checkUpdate.getFactoryUrl();
        String xpath = checkUpdate.getFactoryXpath();

        CheckUpdateResp.CheckUpdateRespBuilder builder = CheckUpdateResp.builder();
        builder.update(false);
        SysInfo sysInfo = sysInfoMapper.selectOne(Wrappers.lambdaQuery(SysInfo.class)
                .last("limit 1"));
        if (Objects.nonNull(sysInfo)) {
            String cppVersion = sysInfo.getFactoryVersion();
            String newVersion = JsoupUtil.getValue(checkUrl, xpath, proxyIp, proxyPort);
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
        String proxyIp = settingsService.getCachedSettings().getProxyIp();
        Integer proxyPort = settingsService.getCachedSettings().getProxyPort();
        JllamaConfigProperties.CheckUpdate checkUpdate = jllamaConfigProperties.getCheckUpdate();
        String checkUrl = checkUpdate.getSelfUrl();
        String xpath = checkUpdate.getSelfXpath();

        CheckUpdateResp.CheckUpdateRespBuilder builder = CheckUpdateResp.builder();
        builder.update(false);
        SysInfo sysInfo = sysInfoMapper.selectOne(Wrappers.lambdaQuery(SysInfo.class)
                .last("limit 1"));
        if (Objects.nonNull(sysInfo)) {
            String cppVersion = sysInfo.getSelfVersion();
            String newVersion = JsoupUtil.getValue(checkUrl, xpath, proxyIp, proxyPort);
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
