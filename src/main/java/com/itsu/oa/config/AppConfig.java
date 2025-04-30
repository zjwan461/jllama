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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@MapperScan(basePackages = "com.itsu.oa.mapper")
@EnableTransactionManagement(proxyTargetClass = true)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableSpringUtil
@EnableScheduling
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
    public ApplicationRunner applicationRunner(SysInfoMapper sysInfoMapper, BeanFactory beanFactory) {
        return args -> {
            SysInfo sysInfo = sysInfoMapper.selectOne(Wrappers.lambdaQuery(SysInfo.class).last("limit 1"));
            if (sysInfo != null) {
                BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
                BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(SysInfo.class);
                beanDefinitionBuilder.addPropertyValue("platform", sysInfo.getPlatform())
                        .addPropertyValue("osArch", sysInfo.getOsArch())
                        .addPropertyValue("gpuPlatform", sysInfo.getGpuPlatform())
                        .setScope(BeanDefinition.SCOPE_SINGLETON);
                AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
                registry.registerBeanDefinition("sysInfo", beanDefinition);
                System.out.println(SpringUtil.getBean("sysInfo", SysInfo.class));
            }
        };
    }
}
