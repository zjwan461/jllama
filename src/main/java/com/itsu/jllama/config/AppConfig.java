package com.itsu.jllama.config;

import cn.hutool.extra.spring.EnableSpringUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.system.SystemUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.itsu.jllama.core.component.MessageQueue;
import com.itsu.jllama.core.sys.Platform;
import com.itsu.jllama.entity.SysInfo;
import com.itsu.jllama.mapper.SysInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.io.IOException;

@Configuration
@MapperScan(basePackages = "com.itsu.jllama.mapper")
@EnableTransactionManagement(proxyTargetClass = true)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableSpringUtil
@EnableScheduling
@Slf4j
public class AppConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.H2)); // 如果配置多个插件, 切记分页最后添加
        // 如果有多数据源可以不配具体类型, 否则都建议配上具体的 DbType
        return interceptor;
    }

    @Bean
    public MessageQueue messageQueue() {
        return new MessageQueue();
    }

    @Bean
    public ApplicationRunner applicationRunner(SysInfoMapper sysInfoMapper, BeanFactory beanFactory, Environment environment) {
        return args -> {
            SysInfo sysInfo = sysInfoMapper.selectOne(Wrappers.lambdaQuery(SysInfo.class).last("limit 1"));
            if (sysInfo != null) {
                BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
                BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(SysInfo.class);
                beanDefinitionBuilder.addPropertyValue("platform", sysInfo.getPlatform())
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
            try {
                if (Boolean.TRUE.equals(environment.getProperty("startbrowser", Boolean.class))) {
                    String osName = SystemUtil.getOsInfo().getName();
                    Platform platform = Platform.match(osName);
                    openBrowser("http://127.0.0.1:" + environment.getProperty("server.port") + "/app", platform);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        };
    }

    public void openBrowser(String url, Platform platform) {

        try {
            ProcessBuilder processBuilder = null;
            if (platform == Platform.WINDOWS) {
                processBuilder = new ProcessBuilder("cmd", "/c", "start", url);
            } else if (platform == Platform.MAC) {
                processBuilder = new ProcessBuilder("open", url);
            } else {
                log.warn("当前操作系统不支持打开浏览器");
                return;
            }
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.info("打开浏览器时出现错误，退出码: {}", exitCode);
            }
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e);
        }

    }
}
