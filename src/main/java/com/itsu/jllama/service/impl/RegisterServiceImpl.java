package com.itsu.jllama.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.system.SystemUtil;
import com.itsu.jllama.config.JllamaConfigProperties;
import com.itsu.jllama.controller.req.RegisterReq;
import com.itsu.jllama.core.sys.GpuPlatform;
import com.itsu.jllama.core.sys.Platform;
import com.itsu.jllama.entity.Settings;
import com.itsu.jllama.entity.SysInfo;
import com.itsu.jllama.entity.User;
import com.itsu.jllama.mapper.SysInfoMapper;
import com.itsu.jllama.mapper.UserMapper;
import com.itsu.jllama.service.RegisterService;
import com.itsu.jllama.service.SettingsService;
import com.itsu.jllama.util.CudaUtil;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
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
        } else {
            settings.setGpuFlag(false);
            String llamaCpuDir = jllamaConfigProperties.getLlamaCpuDir();
            String abbr = platform.getAbbr();
            settings.setLlamaCppDir(StrUtil.replace(llamaCpuDir, "%platform%", abbr));
        }
        settings.setModelSaveDir(jllamaConfigProperties.getModel().getSaveDir());
        settings.setLogSaveDir(jllamaConfigProperties.getLlamaLogDir());
        settingsService.save(settings);

        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) SpringUtil.getBeanFactory();
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(SysInfo.class);
        beanDefinitionBuilder
                .addPropertyValue("id", sysInfo.getId())
                .addPropertyValue("createTime", sysInfo.getCreateTime())
                .addPropertyValue("updateTime", sysInfo.getUpdateTime())
                .addPropertyValue("platform", sysInfo.getPlatform())
                .addPropertyValue("osArch", sysInfo.getOsArch())
                .addPropertyValue("gpuPlatform", sysInfo.getGpuPlatform())
                .addPropertyValue("cppVersion", sysInfo.getCppVersion())
                .addPropertyValue("factoryVersion", sysInfo.getFactoryVersion())
                .addPropertyValue("selfVersion", sysInfo.getSelfVersion())
                .setScope(BeanDefinition.SCOPE_SINGLETON);
        AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        registry.registerBeanDefinition("sysInfo", beanDefinition);
        System.out.println(SpringUtil.getBean("sysInfo", SysInfo.class));
    }
}
