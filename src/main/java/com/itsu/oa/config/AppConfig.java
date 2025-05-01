package com.itsu.oa.config;

import cn.hutool.extra.spring.EnableSpringUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.itsu.oa.core.component.MessageQueue;
import com.itsu.oa.entity.SysInfo;
import com.itsu.oa.mapper.SysInfoMapper;
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

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
@MapperScan(basePackages = "com.itsu.oa.mapper")
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
                    openBrowser("http://127.0.0.1:" + environment.getProperty("server.port") + "/app");
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        };
    }

    public void openBrowser(String url) {
        // 检查当前系统是否支持 Desktop 类
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                // 创建 URI 对象
                URI uri = new URI(url);
                // 打开默认浏览器并访问指定的 URL
                Desktop.getDesktop().browse(uri);
            } catch (URISyntaxException | IOException e) {
                // 处理异常
                log.error(e.getMessage(), e);
            }
        } else {
            log.info("当前系统不支持使用 Desktop 类打开浏览器。");
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("open", url);
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
}
