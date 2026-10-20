package org.miaosha.miaosha.common.config;

import org.miaosha.miaosha.common.filter.AdminAuthFilter;
import org.miaosha.miaosha.common.filter.LoginAuthFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

@Configuration
public class FilterConfig {

    /**
     * 登录认证Filter，顺序1最先执行
     */
    @Bean
    public FilterRegistrationBean<LoginAuthFilter> loginFilterRegistration(LoginAuthFilter loginAuthFilter) {
        FilterRegistrationBean<LoginAuthFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(loginAuthFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1); // order越小越先执行
        return registrationBean;
    }

    /**
     * 管理员权限Filter，顺序2
     */
    @Bean
    public FilterRegistrationBean<AdminAuthFilter> adminFilterRegistration(AdminAuthFilter adminAuthFilter) {
        FilterRegistrationBean<AdminAuthFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(adminAuthFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(2);
        return registrationBean;
    }
}

