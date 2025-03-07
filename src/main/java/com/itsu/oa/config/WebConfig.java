package com.itsu.oa.config;

import com.itsu.oa.core.filter.AuthFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.DelegatingFilterProxy;

@Configuration
public class WebConfig {

    @Bean
    public FilterRegistrationBean<DelegatingFilterProxy> delegatingFilterProxy() {
        FilterRegistrationBean<DelegatingFilterProxy> registrationBean = new FilterRegistrationBean<>();
        DelegatingFilterProxy filter = new DelegatingFilterProxy();
        filter.setTargetFilterLifecycle(true);
        filter.setTargetBeanName("authFilter");
        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns("/api/**");
        return registrationBean;
    }

    @Bean
    public AuthFilter authFilter() {
        return new AuthFilter();
    }
}
