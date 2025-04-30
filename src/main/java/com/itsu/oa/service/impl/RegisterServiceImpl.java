package com.itsu.oa.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.system.SystemUtil;
import com.itsu.oa.config.JllamaConfigProperties;
import com.itsu.oa.controller.req.RegisterReq;
import com.itsu.oa.core.sys.GpuPlatform;
import com.itsu.oa.core.sys.Platform;
import com.itsu.oa.entity.Settings;
import com.itsu.oa.entity.SysInfo;
import com.itsu.oa.entity.User;
import com.itsu.oa.mapper.SysInfoMapper;
import com.itsu.oa.mapper.UserMapper;
import com.itsu.oa.service.RegisterService;
import com.itsu.oa.service.SettingsService;
import com.itsu.oa.util.CudaUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class RegisterServiceImpl implements RegisterService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private SysInfoMapper sysInfoMapper;

    @Resource
    private CudaUtil cudaUtil;

    @Resource
    private SettingsService settingsService;

    @Resource
    private JllamaConfigProperties jllamaConfigProperties;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void register(RegisterReq registerReq) {
        User user = new User();
        user.setUsername(registerReq.getUsername());
        user.setPassword(SecureUtil.md5(registerReq.getPassword()));
        user.setEmail(registerReq.getEmail());
        user.setRole("admin");
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        userMapper.insert(user);

        SysInfo sysInfo = new SysInfo();
        String osName = SystemUtil.getOsInfo().getName();
        Platform platform = Platform.match(osName);
        sysInfo.setPlatform(platform.name());
        String arch = SystemUtil.getOsInfo().getArch();
        sysInfo.setOsArch(arch);
        sysInfo.setCreateTime(new Date());
        sysInfo.setUpdateTime(new Date());

        if (platform == Platform.WINDOWS) {
            if (cudaUtil.isCudaAvailable(platform)) {
                sysInfo.setGpuPlatform(GpuPlatform.CUDA.name());
            } else {
                sysInfo.setGpuPlatform(GpuPlatform.CPU.name());
            }
        } else if (platform == Platform.LINUX || platform == Platform.MAC) {
            if (cudaUtil.isCudaAvailable(platform)) {
                sysInfo.setGpuPlatform(GpuPlatform.CUDA.name());
            } else {
                sysInfo.setGpuPlatform(GpuPlatform.CPU.name());
            }
        }
        sysInfoMapper.insert(sysInfo);

        Settings settings = new Settings();
        settings.setId(Settings.DEFAULT_ID);
        settings.setLogLine(50);
        settings.setLogSaveDay(7);
        if (jllamaConfigProperties.getGpu().isEnable()) {
            settings.setGpuFlag(true);
            settings.setLlamaCppDir(jllamaConfigProperties.getGpu().getLlamaDir());
        }else {
            settings.setGpuFlag(false);
            String llamaCpuDir = jllamaConfigProperties.getLlamaCpuDir();
            String abbr = platform.getAbbr();
            settings.setLlamaCppDir(StrUtil.replace(llamaCpuDir, "%platform%", abbr));
        }
        settings.setModelSaveDir(jllamaConfigProperties.getModel().getSaveDir());
        settings.setLogSaveDir(jllamaConfigProperties.getLlamaLogDir());
        settingsService.save(settings);
    }
}
